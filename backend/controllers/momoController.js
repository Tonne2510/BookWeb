const crypto = require("crypto");
const axios = require("axios");

const MOMO_ENDPOINT = "https://test-payment.momo.vn/v2/gateway/api/create";
const MOMO_TIMEOUT_MS = Number(process.env.MOMO_TIMEOUT_MS || 8000);
const MOMO_MAX_ATTEMPTS = Number(process.env.MOMO_MAX_ATTEMPTS || 1);
const MOMO_RETURN_PATH = "/cart/momo-return";
const DEFAULT_MOMO_IPN_URL = "https://webhook.site/b3088a6a-2d1f-48bb-b1be-1a525f2b86ab";

const isLocalUrl = (url = "") => /localhost|127\.0\.0\.1/i.test(url);

const getPublicFrontendBaseUrl = () => {
  const configured = (process.env.FRONTEND_URL || "")
    .split(",")
    .map((origin) => origin.trim())
    .filter(Boolean);

  const firstPublicOrigin = configured.find((origin) => !isLocalUrl(origin));
  return firstPublicOrigin || "https://bookwebt.online";
};

const buildMomoIpnUrl = () => {
  const configuredIpn = (process.env.MOMO_IPN_URL || "").trim();
  if (configuredIpn) {
    return configuredIpn;
  }

  return DEFAULT_MOMO_IPN_URL;
};

const createGatewayRequest = async (requestBody) => {
  let lastError;

  for (let attempt = 1; attempt <= MOMO_MAX_ATTEMPTS; attempt += 1) {
    try {
      const response = await axios.post(MOMO_ENDPOINT, requestBody, {
        headers: { "Content-Type": "application/json" },
        timeout: MOMO_TIMEOUT_MS,
      });
      return response;
    } catch (error) {
      lastError = error;
      const status = error.response?.status;
      const shouldRetry = status >= 500 || error.code === "ECONNABORTED" || !status;
      if (!shouldRetry || attempt === MOMO_MAX_ATTEMPTS) {
        throw lastError;
      }
    }
  }

  throw lastError;
};

const createMoMoPayment = async (req, res) => {
  try {
    const amount = Number(req.body.amount) || 50000;

    // Bộ KEY test của MoMo (sandbox)
    const partnerCode = "MOMO";
    const accessKey = "F8BBA842ECF85";
    const secretKey = "K951B6PE1waDMi640xX08PD3vg6EkVlz";

    const orderInfo = "Thanh toan don hang BookWeb";
    const fallbackReturnUrl = `${getPublicFrontendBaseUrl()}${MOMO_RETURN_PATH}`;
    const requestedRedirectUrl = (req.body.redirectUrl || "").trim();
    const redirectUrl = requestedRedirectUrl || fallbackReturnUrl;
    const ipnUrl = buildMomoIpnUrl();
    const requestType = "captureWallet";
    const extraData = "";

    // Xóa dấu gạch ngang khỏi orderId để MoMo sandbox chấp nhận
    const rawOrderId = req.body.orderId || "";
    const orderId = (rawOrderId ? rawOrderId.replace(/-/g, "") : (partnerCode + Date.now())).substring(0, 50);
    const requestId = (partnerCode + Date.now()).substring(0, 50);

    // Mã hóa chữ ký
    const rawSignature = `accessKey=${accessKey}&amount=${amount}&extraData=${extraData}&ipnUrl=${ipnUrl}&orderId=${orderId}&orderInfo=${orderInfo}&partnerCode=${partnerCode}&redirectUrl=${redirectUrl}&requestId=${requestId}&requestType=${requestType}`;
    const signature = crypto
      .createHmac("sha256", secretKey)
      .update(rawSignature)
      .digest("hex");

    // Đóng gói Data
    const requestBody = {
      partnerCode: partnerCode,
      accessKey: accessKey,
      requestId: requestId,
      amount: amount,
      orderId: orderId,
      orderInfo: orderInfo,
      redirectUrl: redirectUrl,
      ipnUrl: ipnUrl,
      extraData: extraData,
      requestType: requestType,
      signature: signature,
      lang: "vi",
    };

    const response = await createGatewayRequest(requestBody);

    if (response.data && response.data.payUrl) {
      res.status(200).json({ payUrl: response.data.payUrl });
    } else {
      console.error("MoMo trả về lỗi:", response.data);
      res.status(400).json({ message: "MoMo từ chối tạo link. (Xem log)" });
    }
  } catch (error) {
    const momoErr = error.response ? error.response.data : null;
    console.error("Lỗi MoMo:", momoErr || error.message);
    const msg = momoErr?.message || momoErr?.localMessage || error.message || "Không thể kết nối MoMo.";
    res.status(500).json({ message: msg });
  }
};

const handleMoMoIpn = async (req, res) => {
  try {
    // Acknowledge MoMo callback quickly to prevent payment status failures.
    return res.status(204).send();
  } catch (error) {
    return res.status(204).send();
  }
};

module.exports = { createMoMoPayment, handleMoMoIpn };
