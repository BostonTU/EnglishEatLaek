
package com.example.englisheatleak;

import java.util.ArrayList;
import java.util.List;

public class VocabularyDatabase {

    private static final List<Vocabulary> words = new ArrayList<>();

    static {
        int id = 1;

        // =========================
        // EASY - 50 WORDS
        // =========================
        String[][] easy = {
                {"Abandon", "ละทิ้ง"},
                {"Accept", "ยอมรับ"},
                {"Agree", "เห็นด้วย"},
                {"Arrive", "มาถึง"},
                {"Borrow", "ยืม"},
                {"Buy", "ซื้อ"},
                {"Call", "โทร"},
                {"Cancel", "ยกเลิก"},
                {"Change", "เปลี่ยนแปลง"},
                {"Choose", "เลือก"},
                {"Clean", "ทำความสะอาด"},
                {"Close", "ปิด"},
                {"Company", "บริษัท"},
                {"Complete", "ทำให้เสร็จ"},
                {"Customer", "ลูกค้า"},
                {"Deliver", "จัดส่ง"},
                {"Department", "แผนก"},
                {"Design", "ออกแบบ"},
                {"Discuss", "อภิปราย"},
                {"Employee", "พนักงาน"},
                {"Enjoy", "เพลิดเพลิน"},
                {"Enter", "เข้าไป"},
                {"Explain", "อธิบาย"},
                {"Finish", "เสร็จสิ้น"},
                {"Help", "ช่วยเหลือ"},
                {"Important", "สำคัญ"},
                {"Improve", "ปรับปรุง"},
                {"Invite", "เชิญ"},
                {"Job", "งาน"},
                {"Join", "เข้าร่วม"},
                {"Leave", "ออกจาก"},
                {"Meeting", "การประชุม"},
                {"Message", "ข้อความ"},
                {"Office", "สำนักงาน"},
                {"Order", "สั่งซื้อ"},
                {"Pay", "จ่ายเงิน"},
                {"Plan", "วางแผน"},
                {"Prepare", "เตรียม"},
                {"Price", "ราคา"},
                {"Problem", "ปัญหา"},
                {"Receive", "ได้รับ"},
                {"Report", "รายงาน"},
                {"Request", "ร้องขอ"},
                {"Return", "ส่งคืน"},
                {"Schedule", "กำหนดการ"},
                {"Service", "บริการ"},
                {"Staff", "พนักงาน"},
                {"Travel", "เดินทาง"},
                {"Visit", "เยี่ยมชม"},
                {"Work", "ทำงาน"}
        };

        // =========================
        // MEDIUM - 50 WORDS
        // =========================
        String[][] medium = {
                {"Achieve", "บรรลุผล"},
                {"Advertise", "โฆษณา"},
                {"Application", "ใบสมัคร"},
                {"Appointment", "การนัดหมาย"},
                {"Approve", "อนุมัติ"},
                {"Arrange", "จัดเตรียม"},
                {"Available", "พร้อมใช้งาน"},
                {"Benefit", "ผลประโยชน์"},
                {"Budget", "งบประมาณ"},
                {"Candidate", "ผู้สมัคร"},
                {"Confirm", "ยืนยัน"},
                {"Consider", "พิจารณา"},
                {"Contract", "สัญญา"},
                {"Convenient", "สะดวก"},
                {"Cooperate", "ร่วมมือ"},
                {"Decrease", "ลดลง"},
                {"Demand", "ความต้องการ"},
                {"Describe", "บรรยาย"},
                {"Develop", "พัฒนา"},
                {"Effective", "มีประสิทธิภาพ"},
                {"Equipment", "อุปกรณ์"},
                {"Establish", "ก่อตั้ง"},
                {"Experience", "ประสบการณ์"},
                {"Expand", "ขยาย"},
                {"Expense", "ค่าใช้จ่าย"},
                {"Facility", "สิ่งอำนวยความสะดวก"},
                {"Financial", "ทางการเงิน"},
                {"Guarantee", "รับประกัน"},
                {"Increase", "เพิ่มขึ้น"},
                {"Inform", "แจ้งให้ทราบ"},
                {"Inspect", "ตรวจสอบ"},
                {"Instruction", "คำแนะนำ"},
                {"Interview", "สัมภาษณ์"},
                {"Maintain", "บำรุงรักษา"},
                {"Manage", "จัดการ"},
                {"Offer", "เสนอ"},
                {"Organize", "จัดระเบียบ"},
                {"Participate", "เข้าร่วม"},
                {"Performance", "ผลการปฏิบัติงาน"},
                {"Policy", "นโยบาย"},
                {"Position", "ตำแหน่ง"},
                {"Purchase", "ซื้อ"},
                {"Recommend", "แนะนำ"},
                {"Reduce", "ลด"},
                {"Require", "ต้องการ"},
                {"Responsible", "รับผิดชอบ"},
                {"Select", "คัดเลือก"},
                {"Submit", "ส่งเอกสาร"},
                {"Supply", "จัดหา"},
                {"Training", "การฝึกอบรม"}
        };

        // =========================
        // HARD - 50 WORDS
        // =========================
        String[][] hard = {
                {"Acquisition", "การเข้าซื้อกิจการ"},
                {"Allocate", "จัดสรร"},
                {"Amendment", "การแก้ไขเพิ่มเติม"},
                {"Appraisal", "การประเมิน"},
                {"Arbitration", "การอนุญาโตตุลาการ"},
                {"Asset", "สินทรัพย์"},
                {"Authorize", "ให้อำนาจ"},
                {"Bankruptcy", "การล้มละลาย"},
                {"Compensation", "ค่าตอบแทน"},
                {"Compliance", "การปฏิบัติตามกฎ"},
                {"Confidential", "เป็นความลับ"},
                {"Consolidate", "รวมเข้าด้วยกัน"},
                {"Contingency", "เหตุการณ์ที่อาจเกิดขึ้น"},
                {"Credibility", "ความน่าเชื่อถือ"},
                {"Deficit", "การขาดดุล"},
                {"Delegate", "มอบหมายงาน"},
                {"Discrepancy", "ความไม่ตรงกัน"},
                {"Discretion", "ดุลยพินิจ"},
                {"Eligible", "มีคุณสมบัติเหมาะสม"},
                {"Entrepreneur", "ผู้ประกอบการ"},
                {"Expenditure", "รายจ่าย"},
                {"Feasibility", "ความเป็นไปได้"},
                {"Fluctuation", "ความผันผวน"},
                {"Implement", "ดำเนินการ"},
                {"Incentive", "สิ่งจูงใจ"},
                {"Incorporate", "รวมเข้าเป็นส่วนหนึ่ง"},
                {"Indemnity", "การชดใช้ค่าเสียหาย"},
                {"Infrastructure", "โครงสร้างพื้นฐาน"},
                {"Initiative", "ความคิดริเริ่ม"},
                {"Liability", "หนี้สิน"},
                {"Merger", "การควบรวมกิจการ"},
                {"Monetary", "เกี่ยวกับการเงิน"},
                {"Negotiation", "การเจรจาต่อรอง"},
                {"Obligation", "ภาระผูกพัน"},
                {"Procurement", "การจัดซื้อจัดจ้าง"},
                {"Productivity", "ผลิตภาพ"},
                {"Proficiency", "ความเชี่ยวชาญ"},
                {"Reimbursement", "การชดใช้เงิน"},
                {"Remuneration", "ค่าตอบแทน"},
                {"Restructuring", "การปรับโครงสร้าง"},
                {"Revenue", "รายได้"},
                {"Solvency", "ความสามารถในการชำระหนี้"},
                {"Subsidiary", "บริษัทในเครือ"},
                {"Substantial", "มีนัยสำคัญ"},
                {"Sufficient", "เพียงพอ"},
                {"Transaction", "ธุรกรรม"},
                {"Transparent", "โปร่งใส"},
                {"Unprecedented", "ไม่เคยเกิดขึ้นมาก่อน"},
                {"Viable", "สามารถดำเนินการได้"},
                {"Voluntary", "สมัครใจ"}
        };

        // Add Easy words
        for (String[] item : easy) {
            words.add(new Vocabulary(
                    id++, item[0], item[1], "Easy"
            ));
        }

        // Add Medium words
        for (String[] item : medium) {
            words.add(new Vocabulary(
                    id++, item[0], item[1], "Medium"
            ));
        }

        // Add Hard words
        for (String[] item : hard) {
            words.add(new Vocabulary(
                    id++, item[0], item[1], "Hard"
            ));
        }
    }

    // Get all vocabulary
    public static List<Vocabulary> getAllWords() {
        return new ArrayList<>(words);
    }

    // Get vocabulary by difficulty
    public static List<Vocabulary> getWordsByDifficulty(String difficulty) {

        List<Vocabulary> result = new ArrayList<>();

        for (Vocabulary word : words) {
            if (word.getDifficulty().equalsIgnoreCase(difficulty)) {
                result.add(word);
            }
        }

        return result;
    }

    // Get total vocabulary count
    public static int getWordCount() {
        return words.size();
    }

    // Get vocabulary by ID
    public static Vocabulary getWordById(int id) {

        for (Vocabulary word : words) {
            if (word.getId() == id) {
                return word;
            }
        }

        return null;
    }
}
