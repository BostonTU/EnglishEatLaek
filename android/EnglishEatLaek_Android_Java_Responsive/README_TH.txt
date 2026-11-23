EnglishEatLaek - Android Native Java (Responsive + Themes)

มีหน้าจอ:
- Home
- Gameplay
- Confirmation
- Theme picker: System default / Light / Dark (บันทึกด้วย SharedPreferences)

รองรับ:
- Phone และ Tablet โดยตรวจ screenWidthDp >= 600
- แนวตั้ง/แนวนอน (ไม่ล็อก orientation)
- Light / Dark / System default

วิธีเปิด:
1. แตก ZIP
2. Android Studio > Open > เลือกโฟลเดอร์ EnglishEatLaek_Android_Java_Responsive
3. รอ Gradle Sync แล้ว Run บน Emulator

ข้อควรทราบ:
- นี่เป็น starter project แยกจาก repository ทีม
- ภาพตัวละคร/ฉากเป็น placeholder เพราะยังไม่มีการ Export Assets จาก Figma
- สีและองค์ประกอบอิงจากรายละเอียดที่คุยกัน ไม่สามารถรับประกันความเหมือน Figma แบบ pixel-perfect จนกว่าจะมี PNG/Assets และรายละเอียดต้นฉบับ
- Package ปัจจุบันคือ com.example.englisheatlaek หากนำไปใส่โปรเจกต์ทีม ให้ปรับ package/namespace ให้ตรงกัน
- ใช้ Android Gradle Plugin 8.6.1 และ compileSdk 35
