const sequelize = require('./db/connection');
const Book = require('./models/Book');
const Category = require('./models/Category');
const Author = require('./models/Author');
const { v4: uuidv4 } = require('uuid');

const sampleBooks = [
  {
    title: 'Đắc Nhân Tâm',
    slug: 'dac-nhan-tam',
    description: 'Cuốn sách nổi tiếng về nghệ thuật giao tiếp và ảnh hưởng con người',
    coverImage: 'https://images-na.ssl-images-amazon.com/images/P/B01FXMCSWC.01.L.jpg',
    price: 89000,
    discount: 15,
    status: 'active',
    isbn: '978-0-671-02693-8'
  },
  {
    title: 'Thói Quen Của Những Người Thành Công',
    slug: 'thoi-quan-nhung-nguoi-thanh-cong',
    description: 'Khám phá những thói quen hàng ngày của những người thành đạt',
    coverImage: 'https://images-na.ssl-images-amazon.com/images/P/0399159679.01.L.jpg',
    price: 75000,
    discount: 10,
    status: 'active',
    isbn: '978-1-4391-8992-8'
  },
  {
    title: 'Sapiens: Lược Sử Loài Người',
    slug: 'sapiens-luoc-su-loai-nguoi',
    description: 'Hành trình từ thời kỳ đá để đến với thế giới hiện đại',
    coverImage: 'https://images-na.ssl-images-amazon.com/images/P/B00ICN4CW0.01.L.jpg',
    price: 245000,
    discount: 20,
    status: 'active',
    isbn: '978-0-06-231609-7'
  },
  {
    title: 'Tư Duy Nhanh và Chậm',
    slug: 'tu-duy-nhanh-va-cham',
    description: 'Hiểu biết về hai hệ thống tư duy của con người',
    coverImage: 'https://images-na.ssl-images-amazon.com/images/P/B00555X8OA.01.L.jpg',
    price: 155000,
    discount: 12,
    status: 'active',
    isbn: '978-0-374-27563-1'
  },
  {
    title: 'Lập Trình Python Cho Người Mới Bắt Đầu',
    slug: 'lap-trinh-python-nguoi-moi',
    description: 'Hướng dẫn toàn diện Python từ cơ bản đến nâng cao',
    coverImage: 'https://images-na.ssl-images-amazon.com/images/P/B077D31LQV.01.L.jpg',
    price: 199000,
    discount: 15,
    status: 'active',
    isbn: '978-1-491-93966-8'
  },
  {
    title: 'Kinh Tế Từng Bước',
    slug: 'kinh-te-tung-buoc',
    description: 'Giải thích các khái niệm kinh tế phức tạp một cách đơn giản',
    coverImage: 'https://images-na.ssl-images-amazon.com/images/P/B00ZJO6ALU.01.L.jpg',
    price: 125000,
    discount: 8,
    status: 'active',
    isbn: '978-0-316-04996-1'
  },
  {
    title: '4 Nguyên Tắc Của Sự Thành Công',
    slug: '4-nguyen-tac-thanh-cong',
    description: 'Những nguyên tắc vàng để đạt được mục tiêu trong cuộc sống',
    coverImage: 'https://images-na.ssl-images-amazon.com/images/P/B003P2JP5M.01.L.jpg',
    price: 165000,
    discount: 18,
    status: 'active',
    isbn: '978-0-06-190261-0'
  },
  {
    title: 'Trí Tuệ Nhân Tạo - AI Cho Tất Cả',
    slug: 'tri-tue-nhan-tao-ai',
    description: 'Khám phá những bí mật của trí tuệ nhân tạo thời hiện đại',
    coverImage: 'https://images-na.ssl-images-amazon.com/images/P/B07XFD8GZT.01.L.jpg',
    price: 289000,
    discount: 25,
    status: 'active',
    isbn: '978-0-393-63424-1'
  },
  {
    title: 'Lịch Sử Ngắn Gọn Về Mọi Thứ',
    slug: 'lich-su-ngan-gon-moi-thu',
    description: 'Những bậc thang lịch sử quan trọng nhất của nhân loại',
    coverImage: 'https://images-na.ssl-images-amazon.com/images/P/B079XRGTSP.01.L.jpg',
    price: 198000,
    discount: 15,
    status: 'active',
    isbn: '978-0-393-35857-4'
  },
  {
    title: 'Sức Mạnh Tập Trung',
    slug: 'suc-manh-tap-trung',
    description: 'Cách để tập trung tối đa và tăng năng suất công việc',
    coverImage: 'https://images-na.ssl-images-amazon.com/images/P/B00CRV0J8K.01.L.jpg',
    price: 145000,
    discount: 10,
    status: 'active',
    isbn: '978-0-316-23712-8'
  },
  {
    title: 'CSS3 & HTML5 Chuyên Sâu',
    slug: 'css3-html5-chuyen-sau',
    description: 'Làm chủ công nghệ web frontend hiện đại',
    coverImage: 'https://images-na.ssl-images-amazon.com/images/P/B01BJ4V15S.01.L.jpg',
    price: 169000,
    discount: 12,
    status: 'active',
    isbn: '978-1-491-94954-4'
  },
  {
    title: 'Tâm Lý Học Con Người',
    slug: 'tam-ly-hoc-con-nguoi',
    description: 'Hiểu biết về bản chất tâm lý của con người',
    coverImage: 'https://images-na.ssl-images-amazon.com/images/P/B00IHXQRFU.01.L.jpg',
    price: 135000,
    discount: 14,
    status: 'active',
    isbn: '978-0-19-953018-0'
  }
];

const sampleCategories = [
  { name: 'Tự Phát Triển', slug: 'tu-phat-trien' },
  { name: 'Kinh Doanh & Tài Chính', slug: 'kinh-doanh-tai-chinh' },
  { name: 'Công Nghệ', slug: 'cong-nghe' },
  { name: 'Lập Trình', slug: 'lap-trinh' },
  { name: 'Lịch Sử', slug: 'lich-su' },
  { name: 'Tâm Lý Học', slug: 'tam-ly-hoc' },
  { name: 'Khoa Học', slug: 'khoa-hoc' },
  { name: 'Tiểu Thuyết', slug: 'tieu-thuyet' },
  { name: 'Du Lịch', slug: 'du-lich' },
  { name: 'Ẩm Thực', slug: 'am-thuc' }
];

const sampleAuthors = [
  { name: 'Dale Carnegie', slug: 'dale-carnegie', biography: 'Tác giả nổi tiếng về phát triển bản thân và giao tiếp' },
  { name: 'Sean Covey', slug: 'sean-covey', biography: 'Tác giả và diễn giả về thói quen thành công' },
  { name: 'Yuval Noah Harari', slug: 'yuval-noah-harari', biography: 'Nhà sử học người Israel nổi tiếng' },
  { name: 'Daniel Kahneman', slug: 'daniel-kahneman', biography: 'Giáo sư tâm lý học và nhà kinh tế học' },
  { name: 'Mark Lutz', slug: 'mark-lutz', biography: 'Chuyên gia lập trình Python' },
  { name: 'Gregory Mankiw', slug: 'gregory-mankiw', biography: 'Nhà kinh tế học hàng đầu' },
  { name: 'Jim Collins', slug: 'jim-collins', biography: 'Tác giả về lãnh đạo và quản lý' },
  { name: 'Andrew Ng', slug: 'andrew-ng', biography: 'Chuyên gia trí tuệ nhân tạo' },
  { name: 'Nigel Smith', slug: 'nigel-smith', biography: 'Tác giả về lịch sử loài người' },
  { name: 'Cal Newport', slug: 'cal-newport', biography: 'Chuyên gia về tập trung và năng suất' }
];

async function seedBooks() {
  try {
    await sequelize.authenticate();
    console.log('✅ Database connected');

    // Check if books already exist
    const existingBooks = await Book.count();
    if (existingBooks > 0) {
      console.log('⚠️  Books already seeded');
      process.exit(0);
    }

    // Create categories
    const categories = await Category.bulkCreate(sampleCategories);
    console.log(`✅ ${categories.length} categories created`);

    // Create authors
    const authors = await Author.bulkCreate(sampleAuthors);
    console.log(`✅ ${authors.length} authors created`);

    // Create books with random author and category associations
    const bookPromises = sampleBooks.map(async (book, index) => {
      const finalPrice = Math.floor(book.price * (1 - book.discount / 100));
      return Book.create({
        ...book,
        finalPrice,
        authorId: authors[index % authors.length].id,
        categoryId: categories[index % categories.length].id,
        rating: (Math.random() * 2 + 3.5).toFixed(1),
        stock: Math.floor(Math.random() * 50) + 10
      });
    });

    const createdBooks = await Promise.all(bookPromises);
    console.log(`✅ ${createdBooks.length} books created`);

    console.log('\n📚 Sample Data Seeded Successfully!');
    console.log(`   - ${categories.length} categories`);
    console.log(`   - ${authors.length} authors`);
    console.log(`   - ${createdBooks.length} books`);

    process.exit(0);
  } catch (error) {
    console.error('❌ Error seeding books:', error);
    process.exit(1);
  }
}

seedBooks();
