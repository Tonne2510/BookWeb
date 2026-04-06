# HƯỚNG DẪN DEPLOY BOOKWEB LÊN ORACLE CLOUD (MIỄN PHÍ)

## Tổng chi phí: ~$7.48/NĂM (chỉ tiền domain!)

| Hạng mục | Chi phí | Ghi chú |
|---|---|---|
| Domain `.org` (Namecheap) | $7.48/năm | ~185.000 VNĐ/năm |
| Server (Oracle Always Free) | $0/tháng | MIỄN PHÍ vĩnh viễn |
| SSL (Let's Encrypt) | $0 | Tự động gia hạn |
| **TỔNG** | **~$7.48/năm** | **~15.000 VNĐ/tháng** |

---

## PHẦN 1: MUA TÊN MIỀN TRÊN NAMECHEAP

### Bước 1.1: Tạo tài khoản Namecheap
1. Truy cập: https://www.namecheap.com/
2. Nhấn **Sign Up** → nhập thông tin
3. Xác nhận email

### Bước 1.2: Tìm và mua domain
1. Vào trang chủ, gõ tên domain bạn muốn (ví dụ: `bookweb`) → nhấn Search
2. Chọn `.org` → nhấn **Add to Cart**
3. Vào Cart → Review:
   - **Domain Registration**: 1 year
   - **WhoisGuard**: FREE (bảo vệ thông tin cá nhân)
   - **Auto-Renew**: BẬT (tránh quên gia hạn)
4. Nhấn **Confirm Order** → Thanh toán (VISA/MasterCard/PayPal)

**Giá: $7.48 cho năm đầu tiên (khoảng ~185.000 VNĐ)**

### Bước 1.3: Cấu hình DNS (sau khi có server Oracle)
> ⚠️ LÀM BƯỚC NÀY SAU KHI CÓ IP CỦA ORACLE SERVER (xem Phần 2)

1. Vào **Dashboard → Domain List → Manage** (domain bạn mua)
2. Chọn tab **Advanced DNS**
3. Xóa các records mặc định
4. Thêm 2 records:

| Type | Host | Value | TTL |
|---|---|---|---|
| A Record | `@` | `<IP_ORACLE_SERVER>` | Automatic |
| A Record | `www` | `<IP_ORACLE_SERVER>` | Automatic |

5. Nhấn **Save** → Chờ 5-30 phút để DNS propagate

**Kiểm tra DNS:**
```bash
# Trên máy local
nslookup yourdomain.org
# Phải trả về IP Oracle server
```

---

## PHẦN 2: TẠO SERVER ORACLE CLOUD (MIỄN PHÍ)

### Bước 2.1: Đăng ký Oracle Cloud
1. Truy cập: https://cloud.oracle.com/
2. Nhấn **Sign Up for Free**
3. Nhập thông tin:
   - **Country**: `Vietnam`
   - **Name**: Tên thật (phải khớp với thẻ)
   - **Email**: Email thường dùng
   - **Home Region**: Chọn **Singapore** (gần VN nhất) hoặc **Tokyo**
   
   > ⚠️ **QUAN TRỌNG**: Home Region KHÔNG THỂ đổi sau khi tạo!
   > Chọn **ap-singapore-1** hoặc **ap-tokyo-1** để có latency tốt nhất từ VN

4. Xác nhận email
5. **Nhập thẻ VISA/MasterCard**: Oracle sẽ charge $1 rồi hoàn lại
   - Không bị tính phí nếu dùng Always Free
   - Thẻ chỉ để xác minh, KHÔNG auto-charge

6. Hoàn tất → Nhận $300 trial credit (30 ngày)

### Bước 2.2: Tạo VM Instance (Always Free ARM)

1. Đăng nhập: https://cloud.oracle.com/
2. Vào **Menu (☰) → Compute → Instances**
3. Nhấn **Create Instance**

4. **Cấu hình như sau:**

| Mục | Giá trị |
|---|---|
| **Name** | `bookweb-server` |
| **Placement** | Để mặc định |
| **Image** | **Canonical Ubuntu 22.04** (nhấn Change Image → chọn) |
| **Shape** | Nhấn **Change Shape** → chọn **Ampere** → **VM.Standard.A1.Flex** |
| **OCPUs** | **2** (miễn phí tối đa 4) |
| **Memory (GB)** | **12** (miễn phí tối đa 24) |
| **Boot Volume** | 50 GB (miễn phí tối đa 200 GB) |

5. **Networking:**
   - **VCN**: Tạo mới hoặc chọn có sẵn
   - **Subnet**: Public subnet
   - **Public IPv4 address**: ✅ **Assign a public IPv4 address** (BẮT BUỘC!)

6. **SSH Key:**
   - Chọn **Generate a key pair**
   - **TẢI VỀ CẢ 2 FILE**: `ssh-key-*.key` (private) và `ssh-key-*.key.pub` (public)
   - **LƯU CẨN THẬN!** Không có cách lấy lại nếu mất

7. Nhấn **Create** → Chờ 1-2 phút để instance "Running"

8. **GHI LẠI Public IP** (ví dụ: `129.213.xxx.xxx`)

### Bước 2.3: Mở Ports 80/443 trên Oracle Cloud

> ⚠️ Oracle Cloud có 2 lớp firewall: **Security List** (cloud) + **iptables** (OS)
> Cần mở CẢ HAI!

**Lớp 1: Security List (OCI Console)**

1. Vào **Menu (☰) → Networking → Virtual Cloud Networks**
2. Nhấn vào VCN → nhấn vào **Public Subnet**
3. Nhấn vào **Security List** (Default Security List)
4. Nhấn **Add Ingress Rules** → Thêm 2 rules:

| Source CIDR | Protocol | Dest Port | Description |
|---|---|---|---|
| `0.0.0.0/0` | TCP | `80` | HTTP |
| `0.0.0.0/0` | TCP | `443` | HTTPS |

5. Nhấn **Add Ingress Rules**

**Lớp 2: iptables (sẽ được deploy.sh xử lý tự động)**

### Bước 2.4: Đăng ký Reserved Public IP (tránh mất IP)

> Mặc định Oracle dùng ephemeral IP (mất khi stop instance). Đổi sang Reserved IP:

1. Vào **Menu (☰) → Networking → IP Management → Reserved Public IPs**
2. Nhấn **Reserve Public IP Address**
   - Name: `bookweb-ip`
3. Quay lại Instance → **Attached VNICs** → nhấn VNIC → **IPv4 Addresses**
4. Nhấn **Edit** trên IP hiện tại → chọn **Reserved Public IP** → chọn IP vừa tạo
5. Nhấn **Update**

**Bây giờ quay lại Namecheap cập nhật DNS với IP này (Bước 1.3)**

---

## PHẦN 3: CÀI ĐẶT VÀ DEPLOY

### Bước 3.1: SSH vào server

```bash
# Trên máy local (PowerShell hoặc Terminal)
# Thay bằng đường dẫn file key và IP thực

# Đặt quyền cho file key (Linux/Mac)
chmod 400 ssh-key-2026-xx-xx.key

# SSH vào server
ssh -i ssh-key-2026-xx-xx.key ubuntu@<IP_ORACLE_SERVER>
```

**Trên Windows (PowerShell):**
```powershell
# Copy file key vào thư mục .ssh
Copy-Item "C:\Users\GMT\Downloads\ssh-key-*.key" "$env:USERPROFILE\.ssh\oracle-key.pem"

# SSH
ssh -i "$env:USERPROFILE\.ssh\oracle-key.pem" ubuntu@<IP_ORACLE_SERVER>
```

### Bước 3.2: Chuẩn bị server (chạy trên Oracle server)

```bash
# Cập nhật hệ thống
sudo apt update && sudo apt upgrade -y

# Cài tools cơ bản
sudo apt install -y git iptables-persistent

# Cài Docker
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER

# QUAN TRỌNG: Logout và login lại
exit
```

**SSH lại vào server, rồi kiểm tra Docker:**
```bash
ssh -i ssh-key-*.key ubuntu@<IP_ORACLE_SERVER>
docker --version    # Docker version 26.x.x
docker compose version  # Docker Compose version v2.x.x
```

### Bước 3.3: Upload code lên server

**Cách 1: Git (khuyến nghị)**
```bash
# Nếu code đã trên GitHub:
git clone https://github.com/YOUR_USERNAME/BookWeb.git
cd BookWeb
```

**Cách 2: SCP (copy trực tiếp)**
```powershell
# Trên Windows PowerShell, trong thư mục BookWeb:
scp -i "$env:USERPROFILE\.ssh\oracle-key.pem" -r . ubuntu@<IP_ORACLE_SERVER>:~/BookWeb/
```

**Cách 3: rsync (nhanh hơn SCP)**
```bash
# Trên Linux/Mac:
rsync -avz --exclude 'node_modules' --exclude 'target' --exclude '.git' \
    -e "ssh -i ssh-key-*.key" \
    . ubuntu@<IP_ORACLE_SERVER>:~/BookWeb/
```

### Bước 3.4: Chạy Deploy!

```bash
# Trên Oracle server
cd ~/BookWeb/deploy

# Chỉnh quyền script
chmod +x deploy.sh

# THAY tên domain thật của bạn vào!
./deploy.sh yourdomain.org your-email@gmail.com
```

**Script sẽ tự động:**
1. ✅ Tạo 4GB swap
2. ✅ Cài Docker (nếu chưa có)
3. ✅ Mở ports 80/443 trên iptables
4. ✅ Cấu hình domain
5. ✅ Tạo password ngẫu nhiên an toàn
6. ✅ Build Docker images cho ARM64 (~10-15 phút lần đầu)
7. ✅ Lấy SSL certificate (Let's Encrypt)
8. ✅ Tạo admin user

### Bước 3.5: Kiểm tra

```bash
# Xem trạng thái containers
docker compose -f docker-compose.prod.yml ps

# Xem logs
docker compose -f docker-compose.prod.yml logs -f

# Xem tài nguyên
docker stats

# Test từ server
curl -I https://yourdomain.org
```

**Truy cập website:** https://yourdomain.org

**Admin login:**
- Email: `admin@bookweb.com`
- Password: `admin123` (ĐỔI NGAY sau khi đăng nhập!)

---

## PHẦN 4: CẬP NHẬT OAUTH CALLBACK URLs

Sau khi deploy, cập nhật callback URLs cho Google và GitHub OAuth:

### Google OAuth
1. Vào: https://console.cloud.google.com/apis/credentials
2. Chỉnh OAuth 2.0 Client:
   - **Authorized redirect URIs**: `https://yourdomain.org/auth/google/callback`
   - **Authorized JavaScript origins**: `https://yourdomain.org`

### GitHub OAuth  
1. Vào: https://github.com/settings/developers
2. Chỉnh OAuth App:
   - **Homepage URL**: `https://yourdomain.org`
   - **Authorization callback URL**: `https://yourdomain.org/auth/github/callback`

---

## PHẦN 5: BẢO TRÌ & QUẢN LÝ

### Các lệnh thường dùng

```bash
# SSH vào server
ssh -i oracle-key.pem ubuntu@<IP_ORACLE_SERVER>

# Xem trạng thái
cd ~/BookWeb/deploy
docker compose -f docker-compose.prod.yml ps

# Xem logs
docker compose -f docker-compose.prod.yml logs -f
docker compose -f docker-compose.prod.yml logs backend -f   # Chỉ backend
docker compose -f docker-compose.prod.yml logs frontend -f  # Chỉ frontend

# Restart tất cả
docker compose -f docker-compose.prod.yml restart

# Restart 1 service
docker compose -f docker-compose.prod.yml restart backend

# Cập nhật code mới
cd ~/BookWeb
git pull
cd deploy
docker compose -f docker-compose.prod.yml up -d --build

# Xem disk usage
df -h
docker system df

# Dọn dẹp Docker (giải phóng disk)
docker system prune -af
```

### Giám sát tài nguyên

```bash
# RAM & CPU real-time
docker stats

# Disk usage
df -h

# Xem swap
free -h

# Xem top processes
htop
```

### Auto-restart khi reboot

Docker đã được cấu hình `restart: always`, nên tất cả containers sẽ tự restart khi server reboot.

### SSL tự động gia hạn

Certbot container tự gia hạn SSL mỗi 12 giờ. Không cần làm gì.

Kiểm tra thủ công:
```bash
docker compose -f docker-compose.prod.yml run --rm certbot certificates
```

---

## PHẦN 6: XỬ LÝ SỰ CỐ

### Oracle "reclaim" instance (thu hồi VM idle)

⚠️ Oracle có thể thu hồi Always Free instances nếu server idle quá lâu.

**Cách phòng tránh:**
```bash
# Tạo cron job giữ server active
sudo crontab -e

# Thêm dòng này (giữ server busy mỗi 5 phút):
*/5 * * * * curl -s https://yourdomain.org > /dev/null 2>&1
```

### Container bị crash

```bash
# Xem logs
docker compose -f docker-compose.prod.yml logs <service-name>

# Restart
docker compose -f docker-compose.prod.yml restart <service-name>

# Rebuild
docker compose -f docker-compose.prod.yml up -d --build <service-name>
```

### Hết disk

```bash
# Kiểm tra
df -h

# Dọn Docker
docker system prune -af
docker volume prune -f  # ⚠️ SẼ XÓA DATA! Backup trước

# Xem MySQL data size
docker exec bookweb-db du -sh /var/lib/mysql
```

### Database backup

```bash
# Backup
docker exec bookweb-db mysqldump -uroot -p"$DB_PASSWORD" bookweb > backup_$(date +%Y%m%d).sql

# Restore
docker exec -i bookweb-db mysql -uroot -p"$DB_PASSWORD" bookweb < backup_20260401.sql
```

### Không SSH được

1. Kiểm tra instance đang "Running" trên OCI Console
2. Kiểm tra Security List có mở port 22
3. Dùng **Console Connection** trên OCI Console (Cloud Shell)

---

## PHẦN 7: TÓM TẮT KIẾN TRÚC

```
Người dùng
    │
    ▼ (HTTPS :443)
┌─────────────────┐
│   Namecheap DNS  │  yourdomain.org → IP Oracle
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│  Oracle Cloud VM (ARM Ampere A1)         │
│  2 OCPU, 12GB RAM, 50GB SSD             │
│                                          │
│  ┌─────────┐                             │
│  │  Nginx   │ :80 → :443 redirect       │
│  │ (ARM64)  │ :443 → SSL termination    │
│  └────┬─────┘                            │
│       │                                  │
│  ┌────┴──────────────────────┐           │
│  │     /           /graphql   │          │
│  │     │           /auth/*    │          │
│  │     │           /payment/* │          │
│  │     ▼           ▼          │          │
│  │ ┌────────┐ ┌─────────┐    │          │
│  │ │Frontend│ │ Backend  │    │          │
│  │ │ Spring │ │ Node.js  │    │          │
│  │ │ :8080  │ │  :4000   │    │          │
│  │ └────────┘ └────┬─────┘    │          │
│  │                 │          │          │
│  │            ┌────▼─────┐    │          │
│  │            │  MySQL   │    │          │
│  │            │  :3306   │    │          │
│  │            └──────────┘    │          │
│  └────────────────────────────┘          │
│                                          │
│  RAM: ~2.4GB used / 12GB available       │
│  Swap: 4GB configured                    │
└──────────────────────────────────────────┘
```

### Phân bổ tài nguyên

| Service | RAM Limit | Ghi chú |
|---|---|---|
| MySQL | 1GB | innodb_buffer_pool=256MB |
| Backend (Node.js) | 512MB | max-old-space-size=384MB |
| Frontend (Java) | 768MB | Xmx=512MB, Xms=256MB |
| Nginx | 64MB | Reverse proxy + SSL |
| **Tổng** | **~2.4GB** | Còn ~9.6GB RAM trống! |

---

## CHECKLIST NHANH

- [ ] 1. Mua domain `.org` trên Namecheap ($7.48)
- [ ] 2. Đăng ký Oracle Cloud (Free)
- [ ] 3. Tạo VM: Ubuntu 22.04, ARM Ampere A1, 2 OCPU, 12GB RAM
- [ ] 4. Mở ports 80/443 trên Security List
- [ ] 5. Đặt Reserved IP cho instance
- [ ] 6. Cấu hình DNS trên Namecheap (A records → IP Oracle)
- [ ] 7. SSH vào server, cài Docker
- [ ] 8. Upload code BookWeb
- [ ] 9. Chạy `./deploy.sh yourdomain.org email@gmail.com`
- [ ] 10. Cập nhật OAuth callback URLs (Google/GitHub)
- [ ] 11. Đổi mật khẩu admin mặc định
- [ ] 12. Setup cron job chống thu hồi
- [ ] 13. Test website hoạt động OK

---

**Tổng thời gian setup: ~30-45 phút**
**Chi phí hàng tháng: $0 (chỉ $7.48/năm cho domain)**
