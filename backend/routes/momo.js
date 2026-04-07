const express = require("express");
const router = express.Router();
const { createMoMoPayment, handleMoMoIpn } = require("../controllers/momoController");

// API tạo thanh toán MoMo (POST)
router.post("/create-payment", createMoMoPayment);
router.post("/ipn", handleMoMoIpn);

module.exports = router;
