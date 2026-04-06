#!/bin/bash
# ============================================
# BookWeb - Deploy Script for Hetzner Cloud
# Tối ưu cho CAX11 (4GB RAM / x86_64)
# Chạy trên Ubuntu 22.04
# ============================================

set -e

DOMAIN="${1:-yourdomain.org}"
EMAIL="${2:-admin@yourdomain.org}"

echo "========================================"
echo "  BookWeb - Hetzner Cloud Deploy"
echo "  Domain: $DOMAIN"
echo "  Email:  $EMAIL"
echo "========================================"

# --- 1. Setup swap (2GB cho 4GB RAM server) ---
if ! swapon --show | grep -q "/swapfile"; then
    echo "[1/7] Setting up 2GB swap..."
    sudo fallocate -l 2G /swapfile
    sudo chmod 600 /swapfile
    sudo mkswap /swapfile
    sudo swapon /swapfile
    echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
    sudo sysctl vm.swappiness=10
    echo 'vm.swappiness=10' | sudo tee -a /etc/sysctl.conf
    echo "  Swap 2GB activated."
else
    echo "[1/7] Swap already configured."
fi

# --- 2. Cài Docker nếu chưa có ---
if ! command -v docker &> /dev/null; then
    echo "[2/7] Installing Docker for ARM64..."
    curl -fsSL https://get.docker.com | sh
    sudo usermod -aG docker $USER
    sudo systemctl enable docker
    sudo systemctl start docker
    echo "  Docker installed. Logging out may be needed..."
    # Tiếp tục với sudo nếu cần
fi

if ! docker compose version &> /dev/null; then
    echo "  Installing Docker Compose plugin..."
    sudo apt-get update
    sudo apt-get install -y docker-compose-plugin
fi

echo "[2/7] Docker OK ($(docker --version))"

# --- 3. Mở firewall (UFW cho Hetzner) ---
echo "[3/7] Configuring firewall..."
sudo ufw allow 22/tcp >/dev/null 2>&1 || true
sudo ufw allow 80/tcp >/dev/null 2>&1 || true
sudo ufw allow 443/tcp >/dev/null 2>&1 || true
sudo ufw --force enable >/dev/null 2>&1 || true
echo "  Ports 22/80/443 opened."

# --- 4. Thay domain trong config ---
echo "[4/7] Configuring domain: $DOMAIN ..."

sed -i "s/yourdomain.com/$DOMAIN/g" nginx/default.conf 2>/dev/null || true
sed -i "s/yourdomain.org/$DOMAIN/g" nginx/default.conf 2>/dev/null || true
sed -i "s/yourdomain.com/$DOMAIN/g" nginx/default-ssl.conf 2>/dev/null || true
sed -i "s/yourdomain.org/$DOMAIN/g" nginx/default-ssl.conf 2>/dev/null || true
sed -i "s|FRONTEND_URL=.*|FRONTEND_URL=https://$DOMAIN|g" .env.prod

echo "  Config updated."

# --- 5. Generate strong passwords if still default ---
if grep -q "CHANGE_ME_STRONG_PASSWORD" .env.prod; then
    echo "[5/7] Generating secure passwords..."
    DB_PASS=$(openssl rand -base64 24 | tr -dc 'a-zA-Z0-9' | head -c 32)
    JWT_SEC=$(openssl rand -base64 48 | tr -dc 'a-zA-Z0-9' | head -c 64)
    sed -i "s/CHANGE_ME_STRONG_PASSWORD_HERE/$DB_PASS/g" .env.prod
    sed -i "s/CHANGE_ME_RANDOM_64_CHARS/$JWT_SEC/g" .env.prod
    echo "  ✅ Generated secure DB_PASSWORD and JWT_SECRET."
    echo "  📝 DB_PASSWORD: $DB_PASS  (LƯU LẠI!)"
else
    echo "[5/7] Passwords already configured."
fi

# --- 6. Build & Start (HTTP mode first) ---
echo "[6/7] Building containers (lần đầu sẽ lâu ~5-10 phút)..."
docker compose -f docker-compose.prod.yml build --no-cache
echo "  Starting services..."
docker compose -f docker-compose.prod.yml up -d

echo "  Waiting for services to be healthy..."
sleep 30

# Kiểm tra services
echo "  Service status:"
docker compose -f docker-compose.prod.yml ps

# --- 7. Get SSL Certificate ---
echo "[7/7] Getting SSL certificate from Let's Encrypt..."
docker compose -f docker-compose.prod.yml run --rm certbot \
    certonly --webroot --webroot-path=/var/www/certbot \
    --email "$EMAIL" --agree-tos --no-eff-email \
    -d "$DOMAIN" -d "www.$DOMAIN"

# Switch to HTTPS config
echo "  Switching to HTTPS..."
cp nginx/default-ssl.conf nginx/default.conf
sed -i "s/yourdomain.com/$DOMAIN/g" nginx/default.conf 2>/dev/null || true
sed -i "s/yourdomain.org/$DOMAIN/g" nginx/default.conf 2>/dev/null || true
docker compose -f docker-compose.prod.yml restart nginx

# Seed admin user
echo "  Seeding admin user..."
docker exec bookweb-backend node seedAdmin.js || true

echo ""
echo "========================================"
echo "  ✅ DEPLOY THÀNH CÔNG!"
echo "  🌐 https://$DOMAIN"
echo "========================================"
echo ""
echo "📊 Tài nguyên sử dụng:"
docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}" 2>/dev/null || true
echo ""
echo "🔑 Admin login:"
echo "  Email: admin@bookweb.com"
echo "  Pass:  admin123 (ĐỔI NGAY!)"
echo ""
echo "📋 Lệnh hữu ích:"
echo "  docker compose -f docker-compose.prod.yml logs -f"
echo "  docker compose -f docker-compose.prod.yml restart"
echo "  docker compose -f docker-compose.prod.yml down"
echo "  docker stats  # Xem tài nguyên real-time"
