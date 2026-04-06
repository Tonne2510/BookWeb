// Polyfill globalThis.crypto for Node.js 18 (required by mbbank)
if (!globalThis.crypto) {
  const { webcrypto } = require('crypto');
  globalThis.crypto = webcrypto;
}

const { MB } = require('mbbank');

// Pending payment watchers: orderId -> { intervalId, timeoutId }
const pendingPayments = new Map();

// SSE clients: orderId -> [res, ...]
const sseClients = new Map();

let mbClient = null;

async function getMBClient() {
  if (!mbClient) {
    mbClient = new MB({
      username: process.env.MB_USERNAME,
      password: process.env.MB_PASSWORD,
    });
    await mbClient.login();
    console.log('[MB] Logged in successfully');
  }
  return mbClient;
}

/**
 * Start watching for a payment matching orderId and amount.
 * Notifies SSE clients when found.
 */
async function watchPayment(orderId, amount) {
  if (pendingPayments.has(orderId)) return;

  console.log(`[MB] Watching payment for order ${orderId}, amount ${amount}`);

  const POLL_INTERVAL = 5000;
  const TIMEOUT_MS = 15 * 60 * 1000;

  // Strip dashes from orderId for matching (bank removes them)
  const orderIdNoDashes = orderId.replace(/-/g, '').toUpperCase();

  const intervalId = setInterval(async () => {
    try {
      const mb = await getMBClient();
      const now = new Date();
      const from = new Date(now.getTime() - 60 * 60 * 1000); // 1 hour ago

      const formatDate = (d) => {
        const dd = String(d.getDate()).padStart(2, '0');
        const mm = String(d.getMonth() + 1).padStart(2, '0');
        const yyyy = d.getFullYear();
        return `${dd}/${mm}/${yyyy}`;
      };

      const result = await mb.getTransactionsHistory({
        accountNumber: process.env.MB_ACCOUNT_NUMBER,
        fromDate: formatDate(from),
        toDate: formatDate(now),
      });

      // Response is an array directly, not { transactionHistoryList: [...] }
      const transactions = Array.isArray(result) ? result : (result?.transactionHistoryList || []);

      for (const tx of transactions) {
        const desc = (tx.transactionDesc || tx.description || tx.addDescription || '').toUpperCase();
        // Bank strips dashes and spaces from description, so match without dashes
        const descNoDashes = desc.replace(/[-\s]/g, '');
        const txAmount = Math.abs(parseFloat(tx.creditAmount || 0));

        const matchContent = descNoDashes.includes('BOOKWEB' + orderIdNoDashes);
        const matchAmount = txAmount === amount && descNoDashes.includes('BOOKWEB');

        if (matchContent || matchAmount) {
          console.log(`[MB] Payment detected for order ${orderId}! Amount: ${txAmount}, Desc: ${desc.substring(0, 80)}`);
          clearInterval(intervalId);
          clearTimeout(timeoutId);
          pendingPayments.delete(orderId);
          notifySSEClients(orderId, { status: 'paid', txInfo: desc });
          return;
        }
      }
    } catch (err) {
      console.error('[MB] Poll error:', err.message);
      if (err.message && (err.message.includes('login') || err.message.includes('session'))) {
        mbClient = null;
      }
    }
  }, POLL_INTERVAL);

  const timeoutId = setTimeout(() => {
    clearInterval(intervalId);
    pendingPayments.delete(orderId);
    notifySSEClients(orderId, { status: 'timeout' });
    console.log(`[MB] Payment watch timeout for order ${orderId}`);
  }, TIMEOUT_MS);

  pendingPayments.set(orderId, { intervalId, timeoutId });
}

function stopWatching(orderId) {
  const entry = pendingPayments.get(orderId);
  if (entry) {
    clearInterval(entry.intervalId);
    clearTimeout(entry.timeoutId);
    pendingPayments.delete(orderId);
  }
}

function addSSEClient(orderId, res) {
  if (!sseClients.has(orderId)) {
    sseClients.set(orderId, []);
  }
  sseClients.get(orderId).push(res);
  console.log(`[MB] SSE client added for order ${orderId}`);
}

function removeSSEClient(orderId, res) {
  if (!sseClients.has(orderId)) return;
  const list = sseClients.get(orderId).filter(r => r !== res);
  if (list.length === 0) sseClients.delete(orderId);
  else sseClients.set(orderId, list);
}

function notifySSEClients(orderId, data) {
  const clients = sseClients.get(orderId) || [];
  const msg = `data: ${JSON.stringify(data)}\n\n`;
  clients.forEach(res => {
    try { res.write(msg); res.end(); } catch (_) {}
  });
  sseClients.delete(orderId);
}

module.exports = { watchPayment, stopWatching, addSSEClient, removeSSEClient };
