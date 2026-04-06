const nodemailer = require('nodemailer');

// Initialize email transporter
const transporter = nodemailer.createTransport({
  host: process.env.SMTP_HOST || 'smtp.gmail.com',
  port: process.env.SMTP_PORT || 587,
  secure: process.env.SMTP_SECURE === 'true' || false,
  auth: {
    user: process.env.SMTP_USER,
    pass: process.env.SMTP_PASSWORD
  }
});

// Test connection
if (process.env.SMTP_USER && process.env.SMTP_PASSWORD) {
  transporter.verify((error, success) => {
    if (error) {
      console.error('❌ Email service error:', error);
    } else {
      console.log('✅ Email service ready');
    }
  });
}

const sendForgotPasswordEmail = async (email, firstName, resetToken, resetUrl) => {
  const mailOptions = {
    from: process.env.SMTP_FROM || 'noreply@bookweb.com',
    to: email,
    subject: 'Đặt lại mật khẩu - BookWeb',
    html: `
      <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f5f5f5;">
        <div style="background-color: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
          
          <!-- Header -->
          <div style="text-align: center; margin-bottom: 30px; border-bottom: 3px solid #0066CC; padding-bottom: 20px;">
            <h1 style="color: #0066CC; margin: 0;">BookWeb</h1>
            <p style="color: #666; margin: 10px 0 0;">Cửa hàng sách trực tuyến</p>
          </div>

          <!-- Content -->
          <div style="margin-bottom: 30px;">
            <p style="color: #333; font-size: 16px;">Xin chào <strong>${firstName},</strong></p>
            
            <p style="color: #666; line-height: 1.6; margin-bottom: 20px;">
              Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn. 
              Nếu bạn không yêu cầu điều này, vui lòng bỏ qua email này.
            </p>

            <p style="color: #666; line-height: 1.6; margin-bottom: 20px;">
              Để đặt lại mật khẩu, vui lòng nhấp vào liên kết bên dưới. 
              Liên kết này sẽ hết hạn sau <strong>24 giờ</strong>.
            </p>

            <!-- Reset Button -->
            <div style="text-align: center; margin-bottom: 20px;">
              <a href="${resetUrl}" style="
                display: inline-block;
                background-color: #0066CC;
                color: white;
                padding: 15px 40px;
                text-decoration: none;
                border-radius: 8px;
                font-weight: bold;
                font-size: 16px;
                transition: background-color 0.3s;
              " onmouseover="this.style.backgroundColor='#1A73E8'" onmouseout="this.style.backgroundColor='#0066CC'">
                Đặt Lại Mật Khẩu
              </a>
            </div>

            <!-- Reset Code -->
            <p style="color: #999; font-size: 12px; margin-top: 20px;">
              Hoặc sao chép mã này vào trình duyệt của bạn:<br/>
              <code style="background-color: #f5f5f5; padding: 10px; display: inline-block; margin-top: 10px; font-family: monospace;">${resetToken}</code>
            </p>
          </div>

          <!-- Security Info -->
          <div style="background-color: #fff3cd; padding: 15px; border-radius: 8px; margin-bottom: 20px; border-left: 4px solid #ffc107;">
            <p style="color: #856404; margin: 0; font-size: 14px;">
              <strong>Lưu ý bảo mật:</strong> Không bao giờ chia sẻ mã reset với bất kỳ ai. 
              BookWeb sẽ không bao giờ yêu cầu bạn gửi mật khẩu qua email.
            </p>
          </div>

          <!-- Footer -->
          <div style="border-top: 1px solid #e0e0e0; padding-top: 20px; text-align: center; color: #999; font-size: 12px;">
            <p style="margin: 10px 0;">
              © 2024 BookWeb. Tất cả quyền được bảo lưu.
            </p>
            <p style="margin: 5px 0;">
              <a href="https://bookweb.com" style="color: #0066CC; text-decoration: none;">Website</a> | 
              <a href="https://bookweb.com/privacy" style="color: #0066CC; text-decoration: none;">Chính sách bảo mật</a>
            </p>
          </div>
        </div>
      </div>
    `
  };

  try {
    await transporter.sendMail(mailOptions);
    return true;
  } catch (error) {
    console.error('Email sending error:', error);
    throw error;
  }
};

const sendResetPasswordConfirmation = async (email, firstName) => {
  const mailOptions = {
    from: process.env.SMTP_FROM || 'noreply@bookweb.com',
    to: email,
    subject: 'Mật khẩu đã được đặt lại thành công - BookWeb',
    html: `
      <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f5f5f5;">
        <div style="background-color: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
          
          <!-- Header -->
          <div style="text-align: center; margin-bottom: 30px; border-bottom: 3px solid #0066CC; padding-bottom: 20px;">
            <h1 style="color: #0066CC; margin: 0;">BookWeb</h1>
          </div>

          <!-- Success Message -->
          <div style="text-align: center; margin-bottom: 30px;">
            <div style="font-size: 48px; margin-bottom: 15px;">✓</div>
            <h2 style="color: #28a745; margin: 0 0 10px 0;">Mật khẩu đã được đặt lại thành công</h2>
            <p style="color: #666;">Bạn đã cập nhật mật khẩu của mình</p>
          </div>

          <!-- Content -->
          <div style="margin-bottom: 30px;">
            <p style="color: #333; font-size: 16px;">Xin chào <strong>${firstName},</strong></p>
            
            <p style="color: #666; line-height: 1.6; margin-bottom: 20px;">
              Đây là thông báo xác nhận rằng mật khẩu của tài khoản BookWeb của bạn đã được thay đổi thành công.
            </p>

            <p style="color: #666; line-height: 1.6; margin-bottom: 20px;">
              Nếu bạn không thực hiện thay đổi này, vui lòng liên hệ với bộ phận hỗ trợ của chúng tôi ngay lập tức.
            </p>

            <!-- Login Button -->
            <div style="text-align: center; margin-bottom: 20px;">
              <a href="${process.env.FRONTEND_URL || 'http://localhost:8080'}/auth/login" style="
                display: inline-block;
                background-color: #0066CC;
                color: white;
                padding: 15px 40px;
                text-decoration: none;
                border-radius: 8px;
                font-weight: bold;
                font-size: 16px;
              ">
                Đăng Nhập Ngay
              </a>
            </div>
          </div>

          <!-- Security Info -->
          <div style="background-color: #d4edda; padding: 15px; border-radius: 8px; border-left: 4px solid #28a745;">
            <p style="color: #155724; margin: 0; font-size: 14px;">
              <strong>Bảo mật tài khoản:</strong> Mật khẩu của bạn được mã hóa và lưu trữ an toàn. 
              Không bao giờ chia sẻ mật khẩu với bất kỳ ai.
            </p>
          </div>

          <!-- Footer -->
          <div style="border-top: 1px solid #e0e0e0; padding-top: 20px; margin-top: 30px; text-align: center; color: #999; font-size: 12px;">
            <p>© 2024 BookWeb. Tất cả quyền được bảo lưu.</p>
          </div>
        </div>
      </div>
    `
  };

  try {
    await transporter.sendMail(mailOptions);
    return true;
  } catch (error) {
    console.error('Email sending error:', error);
    throw error;
  }
};

const sendWelcomeEmail = async (email, firstName) => {
  const mailOptions = {
    from: process.env.SMTP_FROM || 'noreply@bookweb.com',
    to: email,
    subject: 'Chào mừng bạn đến với BookWeb!',
    html: `
      <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f5f5f5;">
        <div style="background-color: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
          
          <div style="text-align: center; margin-bottom: 30px; border-bottom: 3px solid #0066CC; padding-bottom: 20px;">
            <h1 style="color: #0066CC; margin: 0;">Chào mừng đến BookWeb! 📚</h1>
          </div>

          <p style="color: #333; font-size: 16px;">Xin chào <strong>${firstName},</strong></p>
          
          <p style="color: #666; line-height: 1.6;">
            Cảm ơn bạn đã tạo tài khoản tại BookWeb - cửa hàng sách trực tuyến hàng đầu!
          </p>

          <div style="background-color: #f5f5f5; padding: 20px; border-radius: 8px; margin: 20px 0;">
            <h3 style="color: #0066CC; margin-top: 0;">Khám phá những lợi ích của bạn:</h3>
            <ul style="color: #666; line-height: 1.8;">
              <li>🎁 Giảm 20% cho đơn hàng đầu tiên</li>
              <li>⭐ Truy cập độc quyền vào các tiêu đề mới</li>
              <li>💎 Chương trình khách hàng thân thiết</li>
              <li>🚚 Giao hàng miễn phí từ 50,000₫</li>
              <li>↩️ Đổi trả dễ dàng trong 90 ngày</li>
            </ul>
          </div>

          <div style="text-align: center; margin: 30px 0;">
            <a href="${process.env.FRONTEND_URL || 'http://localhost:8080'}/books" style="
              display: inline-block;
              background-color: #0066CC;
              color: white;
              padding: 15px 40px;
              text-decoration: none;
              border-radius: 8px;
              font-weight: bold;
            ">
              Bắt Đầu Mua Sắm
            </a>
          </div>

          <div style="border-top: 1px solid #e0e0e0; padding-top: 20px; text-align: center; color: #999; font-size: 12px;">
            <p>© 2024 BookWeb. Tất cả quyền được bảo lưu.</p>
          </div>
        </div>
      </div>
    `
  };

  try {
    await transporter.sendMail(mailOptions);
    return true;
  } catch (error) {
    console.error('Email sending error:', error);
    throw error;
  }
};

const sendOtpEmail = async (email, otp) => {
  const mailOptions = {
    from: process.env.SMTP_FROM || 'noreply@bookweb.com',
    to: email,
    subject: 'Mã xác nhận đăng ký - BookWeb',
    html: `
      <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f5f5f5;">
        <div style="background-color: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
          <div style="text-align: center; margin-bottom: 30px; border-bottom: 3px solid #0066CC; padding-bottom: 20px;">
            <h1 style="color: #0066CC; margin: 0;">BookWeb</h1>
            <p style="color: #666; margin: 10px 0 0;">Cửa hàng sách trực tuyến</p>
          </div>
          <div style="margin-bottom: 30px;">
            <p style="color: #333; font-size: 16px;">Xin chào,</p>
            <p style="color: #666; line-height: 1.6;">Đây là mã xác nhận để hoàn tất đăng ký tài khoản BookWeb của bạn:</p>
            <div style="text-align: center; margin: 30px 0;">
              <div style="display: inline-block; background-color: #0066CC; color: white; font-size: 36px; font-weight: bold; letter-spacing: 12px; padding: 20px 40px; border-radius: 8px;">
                ${otp}
              </div>
            </div>
            <p style="color: #666; text-align: center;">Mã này có hiệu lực trong <strong>10 phút</strong>.</p>
            <p style="color: #999; font-size: 13px; margin-top: 20px;">Nếu bạn không yêu cầu đăng ký, vui lòng bỏ qua email này.</p>
          </div>
          <div style="border-top: 1px solid #e0e0e0; padding-top: 20px; text-align: center; color: #999; font-size: 12px;">
            <p>© 2024 BookWeb. Tất cả quyền được bảo lưu.</p>
          </div>
        </div>
      </div>
    `
  };

  try {
    await transporter.sendMail(mailOptions);
    return true;
  } catch (error) {
    console.error('OTP email sending error:', error);
    throw error;
  }
};

module.exports = {
  sendForgotPasswordEmail,
  sendResetPasswordConfirmation,
  sendWelcomeEmail,
  sendOtpEmail
};
