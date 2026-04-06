const crypto = require("crypto");
const axios = require("axios");

const createMoMoPayment = async (req, res) => {
  try {
    const amount = Number(req.body.amount) || 50000;

    // Bộ KEY test của MoMo (Chuẩn 100%)
    const partnerCode = "MOMOBKUN20180529";
    const accessKey = "klm05TvNBzhg7h7j";
    const secretKey = "at67qH6mk8w5Y1nAyMoYKMWACiEi2bsa";

    const orderInfo = "Thanh toan don hang BookWeb";
    const redirectUrl = "http://localhost:8080/cart";
    const ipnUrl = "https://webhook.site/b3088a6a-2d1f-48bb-b1be-1a525f2b86ab";
    const requestType = "captureWallet";
    const extraData = "";
    const orderId = partnerCode + new Date().getTime();
    const requestId = orderId;

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

    // GỌI API BẰNG AXIOS VỚI ENDPOINT TEST MỚI
    const response = await axios.post(
      "https://test-payment.momo.vn/v2/gateway/api/create",
      requestBody,
      {
        headers: {
          "Content-Type": "application/json",
        },
      },
    );

    if (response.data && response.data.payUrl) {
      res.status(200).json({ payUrl: response.data.payUrl });
    } else {
      console.error("MoMo trả về lỗi:", response.data);
      res.status(400).json({ message: "MoMo từ chối tạo link. (Xem log)" });
    }
  } catch (error) {
    console.error(
      "Lỗi sập Server MoMo:",
      error.response ? error.response.data : error.message,
    );
    res.status(500).json({ message: "Máy chủ MoMo đang bị lỗi Test!" });
  }
};

module.exports = { createMoMoPayment };
