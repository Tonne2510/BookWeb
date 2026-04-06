const express = require("express");
const router = express.Router();
const { createMoMoPayment } = require("../controllers/momoController");

// API tạo thanh toán MoMo (POST)
router.post("/create-payment", createMoMoPayment);

module.exports = router;
