const crypto = require("crypto");
const axios = require("axios");

const createGatewayRequest = async (requestBody) => {
  const endpoint = "https://test-payment.momo.vn/v2/gateway/api/create";
  let lastError;

  for (let attempt = 1; attempt <= 2; attempt += 1) {
    try {
      const response = await axios.post(endpoint, requestBody, {
        headers: { "Content-Type": "application/json" },
        timeout: 15000,
      });
      return response;
    } catch (error) {
      lastError = error;
      // Retry only for network/timeout/5xx gateway errors
      const status = error.response?.status;
      if (!(status >= 500 || error.code === "ECONNABORTED" || !status) || attempt === 2) {
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
    const redirectUrl = req.body.redirectUrl || "http://localhost:8080/cart/momo-return";
    const ipnUrl = "https://webhook.site/b3088a6a-2d1f-48bb-b1be-1a525f2b86ab";
    const requestType = "captureWallet";
    const extraData = "";
    const orderId = req.body.orderId || (partnerCode + new Date().getTime());
    const requestId = `${orderId}-${Date.now()}`;

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

module.exports = { createMoMoPayment };
