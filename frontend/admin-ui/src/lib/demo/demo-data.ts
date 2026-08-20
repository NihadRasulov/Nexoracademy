function uid(): string {
  return crypto.randomUUID();
}

function daysAgo(n: number): string {
  const d = new Date();
  d.setDate(d.getDate() - n);
  return d.toISOString();
}

function daysFromNow(n: number): string {
  return daysAgo(-n);
}

const userIds = [uid(), uid(), uid(), uid(), uid()];
const courseIds = [uid(), uid(), uid()];
const instructorIds = [uid(), uid()];
const groupIds = [uid(), uid()];
const enrollmentIds = [uid(), uid(), uid()];

export const DEMO_ME = {
  id: userIds[0],
  email: "admin@nexora.academy",
  fullName: "Rəşad Əliyev",
  phone: "+994501234567",
  role: "SYSTEM_ADMIN",
  status: "ACTIVE",
  locale: "az",
  lastLoginAt: daysAgo(0),
};

export const DEMO_HEALTH = { groups: ["liveness", "readiness"], status: "UP" };

export const demoUsers = [
  { ...DEMO_ME, createdAt: daysAgo(400), updatedAt: daysAgo(1) },
  {
    id: userIds[1],
    email: "sabina.mammadova@nexora.academy",
    phone: "+994551112233",
    fullName: "Sabina Məmmədova",
    role: "CONTENT_MANAGER",
    status: "ACTIVE",
    locale: "az",
    lastLoginAt: daysAgo(2),
    createdAt: daysAgo(300),
    updatedAt: daysAgo(2),
  },
  {
    id: userIds[2],
    email: "elvin.huseynov@nexora.academy",
    phone: "+994701112244",
    fullName: "Elvin Hüseynov",
    role: "SALES_CRM",
    status: "ACTIVE",
    locale: "az",
    lastLoginAt: daysAgo(5),
    createdAt: daysAgo(210),
    updatedAt: daysAgo(5),
  },
  {
    id: userIds[3],
    email: "leyla.qasimova@gmail.com",
    phone: null,
    fullName: "Leyla Qasımova",
    role: "STUDENT",
    status: "PENDING_VERIFICATION",
    locale: "az",
    lastLoginAt: null,
    createdAt: daysAgo(3),
    updatedAt: daysAgo(3),
  },
  {
    id: userIds[4],
    email: "kamran.veliyev@gmail.com",
    phone: "+994503332211",
    fullName: "Kamran Vəliyev",
    role: "STUDENT",
    status: "SUSPENDED",
    locale: "az",
    lastLoginAt: daysAgo(40),
    createdAt: daysAgo(150),
    updatedAt: daysAgo(20),
  },
];

export const demoCourses = [
  {
    id: courseIds[0],
    slug: "full-stack-web-development",
    categoryId: 1,
    title: "Full-Stack Veb İnkişafı",
    shortDescription: "React, Node.js və PostgreSQL ilə tam stack veb tətbiqləri qurmağı öyrənin.",
    fullDescription: "Bu kurs frontend-dən backend-ə qədər tam yol xəritəsini əhatə edir.",
    targetAudience: "Proqramlaşdırmaya yeni başlayanlar",
    difficulty: "INTERMEDIATE",
    durationWeeks: 16,
    deliveryFormat: "HYBRID",
    locationText: "Bakı, Nizami filialı",
    basePrice: 1200,
    currency: "AZN",
    pricePeriod: "tam kurs",
    published: true,
    active: true,
    archived: false,
    validFrom: daysAgo(60),
    validUntil: daysFromNow(300),
    relatedCourseIds: [courseIds[1]],
    createdBy: userIds[1],
    createdAt: daysAgo(90),
    updatedAt: daysAgo(4),
  },
  {
    id: courseIds[1],
    slug: "data-analytics-with-python",
    categoryId: 2,
    title: "Python ilə Data Analitikası",
    shortDescription: "Pandas, NumPy və vizuallaşdırma alətləri ilə data təhlili.",
    fullDescription: "Real dataset-lərlə praktiki tapşırıqlar.",
    targetAudience: "Analitiklər və mühəndislər",
    difficulty: "BEGINNER",
    durationWeeks: 8,
    deliveryFormat: "ONLINE",
    locationText: null,
    basePrice: 650,
    currency: "AZN",
    pricePeriod: "tam kurs",
    published: true,
    active: true,
    archived: false,
    validFrom: daysAgo(30),
    validUntil: daysFromNow(200),
    relatedCourseIds: [],
    createdBy: userIds[1],
    createdAt: daysAgo(45),
    updatedAt: daysAgo(10),
  },
  {
    id: courseIds[2],
    slug: "ui-ux-design-fundamentals",
    categoryId: 3,
    title: "UI/UX Dizayn Əsasları",
    shortDescription: "Figma ilə istifadəçi-mərkəzli dizayn prinsipləri.",
    fullDescription: "Wireframe-dən prototype-ə qədər tam proses.",
    targetAudience: "Dizaynerlər və məhsul menecerləri",
    difficulty: "BEGINNER",
    durationWeeks: 6,
    deliveryFormat: "OFFLINE",
    locationText: "Bakı, 28 May filialı",
    basePrice: 500,
    currency: "AZN",
    pricePeriod: "tam kurs",
    published: false,
    active: true,
    archived: false,
    validFrom: daysFromNow(10),
    validUntil: daysFromNow(120),
    relatedCourseIds: [],
    createdBy: userIds[1],
    createdAt: daysAgo(5),
    updatedAt: daysAgo(1),
  },
];

export const demoCategories = [
  { id: 1, slug: "web-development", name: "Veb İnkişafı", parentId: null, sortOrder: 1, active: true },
  { id: 2, slug: "data-science", name: "Data Elmi", parentId: null, sortOrder: 2, active: true },
  { id: 3, slug: "design", name: "Dizayn", parentId: null, sortOrder: 3, active: true },
];

export const demoInstructors = [
  {
    id: instructorIds[0],
    userId: null,
    fullName: "Tural Nağıyev",
    bio: "10+ il təcrübə ilə full-stack mühəndis və mentor.",
    photoUrl: "https://i.pravatar.cc/150?img=12",
    linkedinUrl: "https://linkedin.com/in/tural-nagiyev",
    avgRating: 4.8,
    certifications: [{ name: "AWS Certified Developer", year: 2022 }],
    active: true,
    createdAt: daysAgo(200),
  },
  {
    id: instructorIds[1],
    userId: null,
    fullName: "Aygün Cəfərova",
    bio: "Data scientist və universitet müəllimi.",
    photoUrl: "https://i.pravatar.cc/150?img=32",
    linkedinUrl: "https://linkedin.com/in/aygun-jafarova",
    avgRating: 4.6,
    certifications: [],
    active: true,
    createdAt: daysAgo(180),
  },
];

export const demoCourseInstructors = [
  { courseId: courseIds[0], instructorId: instructorIds[0], role: "lead" },
  { courseId: courseIds[1], instructorId: instructorIds[1], role: "lead" },
  { courseId: courseIds[0], instructorId: instructorIds[1], role: "mentor" },
];

export const demoCourseGroups = [
  {
    id: groupIds[0],
    courseId: courseIds[0],
    groupCode: "FSW-2026-A",
    startDate: daysFromNow(14).slice(0, 10),
    endDate: daysFromNow(126).slice(0, 10),
    registrationDeadline: daysFromNow(10),
    totalSeats: 20,
    reservedSeats: 14,
    status: "OPEN",
    schedule: [{ day: "Monday", time: "19:00-21:00" }],
    createdAt: daysAgo(40),
  },
  {
    id: groupIds[1],
    courseId: courseIds[1],
    groupCode: "PY-2026-B",
    startDate: daysFromNow(30).slice(0, 10),
    endDate: null,
    registrationDeadline: daysFromNow(25),
    totalSeats: 15,
    reservedSeats: 15,
    status: "FULL",
    schedule: [{ day: "Wednesday", time: "18:00-20:00" }],
    createdAt: daysAgo(20),
  },
];

export const demoScholarships = [
  {
    id: 1,
    name: "İstedadlı Tələbə Təqaüdü",
    description: "Yüksək nailiyyətli tələbələr üçün 50% endirim.",
    discountPct: 50,
    maxRecipients: 10,
    validFrom: daysAgo(30).slice(0, 10),
    validUntil: daysFromNow(150).slice(0, 10),
    active: true,
    applications: [],
  },
  {
    id: 2,
    name: "Sosial Dəstək Proqramı",
    description: null,
    discountPct: 30,
    maxRecipients: 25,
    validFrom: null,
    validUntil: null,
    active: true,
    applications: [],
  },
];

export const demoCmsContent = [
  {
    id: 1,
    key: "home.hero.title",
    type: "page",
    title: "Ana səhifə başlığı",
    body: "Gələcəyini quran bacarıqları öyrən.",
    data: null,
    published: true,
    sortOrder: 1,
    updatedBy: userIds[1],
    updatedAt: daysAgo(3),
  },
  {
    id: 2,
    key: "faq.refund-policy",
    type: "faq",
    title: "Geri ödəniş siyasəti",
    body: "Kurs başlamazdan əvvəl ləğv edərsinizsə...",
    data: null,
    published: true,
    sortOrder: 5,
    updatedBy: userIds[1],
    updatedAt: daysAgo(15),
  },
];

export const demoCampaigns = [
  {
    id: uid(),
    name: "Yaz Endirimi 2026",
    bannerImageUrl: "https://placehold.co/600x200",
    ctaUrl: "https://nexora.academy/campaigns/spring",
    discountPct: 20,
    startsAt: daysAgo(5),
    endsAt: daysFromNow(25),
    active: true,
    priority: 1,
    courseIds: [courseIds[0], courseIds[1]],
  },
];

export const demoChatSessions = [
  {
    id: uid(),
    userId: userIds[3],
    leadId: null,
    channel: "website-widget",
    messages: [{ from: "user", text: "Salam, kurs haqqında məlumat almaq istəyirəm." }],
    startedAt: daysAgo(1),
    endedAt: daysAgo(1),
  },
];

export const demoContactSubmissions = [
  {
    id: uid(),
    leadId: null,
    type: "GENERAL_INQUIRY",
    courseId: courseIds[0],
    fullName: "Nərmin Əsgərova",
    email: "nermin.esgerova@gmail.com",
    phone: "+994557778899",
    message: "Full-stack kursu haqqında ətraflı bilgi istəyirəm.",
    preferredTime: daysFromNow(2),
    status: "NEW",
    submittedAt: daysAgo(1),
  },
];

export const demoLeads = [
  {
    id: uid(),
    fullName: "Orxan Bağırov",
    email: "orxan.bagirov@gmail.com",
    phone: "+994703334455",
    courseId: courseIds[1],
    source: "WEBSITE",
    status: "NEW",
    assignedTo: userIds[2],
    consentVersion: "v1",
    consentGivenAt: daysAgo(2),
    duplicateOfLeadId: null,
    activityLog: [],
    createdAt: daysAgo(2),
    updatedAt: daysAgo(2),
  },
  {
    id: uid(),
    fullName: "Günay İsmayılova",
    email: "gunay.ismayilova@gmail.com",
    phone: "+994557001122",
    courseId: courseIds[0],
    source: "REFERRAL",
    status: "CONTACTED",
    assignedTo: userIds[2],
    consentVersion: "v1",
    consentGivenAt: daysAgo(6),
    duplicateOfLeadId: null,
    activityLog: [{ note: "Zəng edildi", at: daysAgo(5) }],
    createdAt: daysAgo(6),
    updatedAt: daysAgo(5),
  },
];

export const demoOAuthAccounts = [
  { id: 1, userId: userIds[3], provider: "google", providerUserId: "109283746192837", linkedAt: daysAgo(10) },
];

export const demoSessions = [
  {
    id: uid(),
    userId: userIds[0],
    type: "SESSION",
    ipAddress: "85.132.10.4",
    userAgent: "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)",
    issuedAt: daysAgo(1),
    expiresAt: daysFromNow(29),
    usedAt: null,
    revokedAt: null,
  },
];

export const demoNotifications = [
  {
    id: uid(),
    userId: userIds[3],
    type: "welcome_email",
    channel: "EMAIL",
    payload: { templateId: "welcome-v2" },
    status: "SENT",
    sentAt: daysAgo(3),
    readAt: null,
    createdAt: daysAgo(3),
  },
];

export const demoKbArticles = [
  {
    id: uid(),
    sourceType: "faq",
    sourceRefId: "faq-1",
    title: "Kursa necə qeydiyyatdan keçim?",
    content: "Kurs səhifəsində 'Qeydiyyatdan keç' düyməsinə klikləyin...",
    active: true,
    updatedAt: daysAgo(20),
  },
];

export const demoCourseReviews = [
  {
    id: 1,
    courseId: courseIds[0],
    userId: userIds[3],
    enrollmentId: enrollmentIds[0],
    rating: 5,
    comment: "Çox faydalı kurs idi, məsləhət görürəm!",
    published: true,
    moderatedBy: userIds[1],
    aiSentiment: { label: "positive", score: 0.96 },
    createdAt: daysAgo(7),
  },
  {
    id: 2,
    courseId: courseIds[1],
    userId: userIds[4],
    enrollmentId: null,
    rating: 4,
    comment: "Yaxşı idi amma bəzi mövzular tez keçildi.",
    published: false,
    moderatedBy: null,
    aiSentiment: null,
    createdAt: daysAgo(1),
  },
];

export const demoGraduateOutcomes = [
  {
    id: 1,
    userId: userIds[4],
    courseId: courseIds[0],
    companyName: "Kapital Bank",
    jobTitle: "Junior Frontend Developer",
    employedAt: daysAgo(20).slice(0, 10),
    salaryBand: "1200-1500 AZN",
    publicStory: true,
    storyText: "Bu kurs sayəsində ilk IT işimi tapdım.",
    createdAt: daysAgo(20),
  },
];

export const demoApplications = [
  {
    id: 1,
    applicationType: 1,
    fullname: "Tural Məmmədov",
    email: "tural.mammadov@gmail.com",
    phone: "+994502223344",
    letter: "Full-Stack Veb İnkişafı kursuna qoşulmaq istəyirəm. Hal-hazırda junior developer olaraq çalışıram və biliklərimi artırmaq istəyirəm. React və Node.js təcrübəm var.",
    cvFilename: "tural_cv.pdf",
    status: "PENDING",
    createdAt: daysAgo(3),
  },
  {
    id: 2,
    applicationType: 1,
    fullname: "Aysel Həsənova",
    email: "aysel.hasanova@outlook.com",
    phone: "+994553334455",
    letter: "Python ilə Data Analitikası kursuna yazılmaq istəyirəm. Statistika təhsilim var və data sahəsində karyera qurmaq niyyətindəyəm. Bu kurs mənə lazım olan praktiki bacarıqları verəcək.",
    cvFilename: null,
    status: "REVIEWED",
    createdAt: daysAgo(7),
  },
  {
    id: 3,
    applicationType: 3,
    fullname: "Elçin Əliyev",
    email: "elchin.aliyev@yahoo.com",
    phone: "+994704445566",
    letter: "Nexora Academy-də UI/UX Dizayn müəllimi vəzifəsinə müraciət edirəm. 8 il dizayn təcrübəm var, Figma və Adobe XD ilə peşəkar səviyyədə işləyirəm. Dərs vermək təcrübəm də mövcuddur.",
    cvFilename: "elchin_cv.docx",
    status: "APPROVED",
    createdAt: daysAgo(14),
  },
];

export const demoAuditLogs = [
  {
    id: 1,
    actorId: userIds[0],
    action: "user.role_changed",
    entityType: "User",
    entityId: userIds[2],
    beforeState: { role: "STUDENT" },
    afterState: { role: "SALES_CRM" },
    traceId: uid(),
    ipAddress: "85.132.10.4",
    createdAt: daysAgo(5),
  },
];

export const demoEnrollments = [
  {
    id: enrollmentIds[0],
    userId: userIds[3],
    groupId: groupIds[0],
    status: "CONFIRMED",
    idempotencyKey: "enr-000001",
    consentVersion: "v1",
    consentGivenAt: daysAgo(10),
    holdExpiresAt: null,
    enrolledAt: daysAgo(10),
    completedAt: null,
    cancelledAt: null,
    cancelReason: null,
  },
  {
    id: enrollmentIds[1],
    userId: userIds[4],
    groupId: groupIds[1],
    status: "PENDING_PAYMENT",
    idempotencyKey: "enr-000002",
    consentVersion: "v1",
    consentGivenAt: daysAgo(2),
    holdExpiresAt: daysFromNow(1),
    enrolledAt: daysAgo(2),
    completedAt: null,
    cancelledAt: null,
    cancelReason: null,
  },
  {
    id: enrollmentIds[2],
    userId: userIds[3],
    groupId: groupIds[1],
    status: "CANCELLED",
    idempotencyKey: "enr-000003",
    consentVersion: "v1",
    consentGivenAt: daysAgo(15),
    holdExpiresAt: null,
    enrolledAt: daysAgo(15),
    completedAt: null,
    cancelledAt: daysAgo(12),
    cancelReason: "İstifadəçi fikrini dəyişdi.",
  },
];

export const demoPayments = [
  {
    id: uid(),
    enrollmentId: enrollmentIds[0],
    method: "CARD",
    amount: 1200,
    currency: "AZN",
    status: "CAPTURED",
    externalTxnId: "txn_8839201",
    idempotencyKey: "pay-000001",
    installments: [],
    refundAmount: 0,
    refundReason: null,
    initiatedAt: daysAgo(10),
    capturedAt: daysAgo(10),
    failureReason: null,
  },
  {
    id: uid(),
    enrollmentId: enrollmentIds[1],
    method: "BANK_TRANSFER",
    amount: 650,
    currency: "AZN",
    status: "PENDING",
    externalTxnId: null,
    idempotencyKey: "pay-000002",
    installments: [{ dueDate: daysFromNow(5), amount: 325 }, { dueDate: daysFromNow(35), amount: 325 }],
    refundAmount: 0,
    refundReason: null,
    initiatedAt: daysAgo(2),
    capturedAt: null,
    failureReason: null,
  },
];
