public class FileInfoTest {
    public static void main(String[] args) {
        FileInfo fileInfo = new FileInfo();
        boolean allTestsPassed = true;

        // Test fileName1
        fileInfo.setFileName1("test1.txt");
        if (!"test1.txt".equals(fileInfo.getFileName1())) {
            System.out.println("FAIL: fileName1 getter/setter");
            allTestsPassed = false;
        }

        // Test saveName1
        fileInfo.setSaveName1("save1.txt");
        if (!"save1.txt".equals(fileInfo.getSaveName1())) {
            System.out.println("FAIL: saveName1 getter/setter");
            allTestsPassed = false;
        }

        // Test savePath1
        fileInfo.setSavePath1("/path/to/file1");
        if (!"/path/to/file1".equals(fileInfo.getSavePath1())) {
            System.out.println("FAIL: savePath1 getter/setter");
            allTestsPassed = false;
        }

        // Test savedate1
        fileInfo.setSavedate1("2025-12-16");
        if (!"2025-12-16".equals(fileInfo.getSavedate1())) {
            System.out.println("FAIL: savedate1 getter/setter");
            allTestsPassed = false;
        }

        // Test saveloc1
        fileInfo.setSaveloc1("location1");
        if (!"location1".equals(fileInfo.getSaveloc1())) {
            System.out.println("FAIL: saveloc1 getter/setter");
            allTestsPassed = false;
        }

        // Test fileName2
        fileInfo.setFileName2("test2.txt");
        if (!"test2.txt".equals(fileInfo.getFileName2())) {
            System.out.println("FAIL: fileName2 getter/setter");
            allTestsPassed = false;
        }

        // Test saveName2
        fileInfo.setSaveName2("save2.txt");
        if (!"save2.txt".equals(fileInfo.getSaveName2())) {
            System.out.println("FAIL: saveName2 getter/setter");
            allTestsPassed = false;
        }

        // Test savePath2
        fileInfo.setSavePath2("/path/to/file2");
        if (!"/path/to/file2".equals(fileInfo.getSavePath2())) {
            System.out.println("FAIL: savePath2 getter/setter");
            allTestsPassed = false;
        }

        // Test savedate2
        fileInfo.setSavedate2("2025-12-17");
        if (!"2025-12-17".equals(fileInfo.getSavedate2())) {
            System.out.println("FAIL: savedate2 getter/setter");
            allTestsPassed = false;
        }

        // Test saveloc2
        fileInfo.setSaveloc2("location2");
        if (!"location2".equals(fileInfo.getSaveloc2())) {
            System.out.println("FAIL: saveloc2 getter/setter");
            allTestsPassed = false;
        }

        // Test fileName3
        fileInfo.setFileName3("test3.txt");
        if (!"test3.txt".equals(fileInfo.getFileName3())) {
            System.out.println("FAIL: fileName3 getter/setter");
            allTestsPassed = false;
        }

        // Test saveName3
        fileInfo.setSaveName3("save3.txt");
        if (!"save3.txt".equals(fileInfo.getSaveName3())) {
            System.out.println("FAIL: saveName3 getter/setter");
            allTestsPassed = false;
        }

        // Test savePath3
        fileInfo.setSavePath3("/path/to/file3");
        if (!"/path/to/file3".equals(fileInfo.getSavePath3())) {
            System.out.println("FAIL: savePath3 getter/setter");
            allTestsPassed = false;
        }

        // Test savedate3
        fileInfo.setSavedate3("2025-12-18");
        if (!"2025-12-18".equals(fileInfo.getSavedate3())) {
            System.out.println("FAIL: savedate3 getter/setter");
            allTestsPassed = false;
        }

        // Test saveloc3
        fileInfo.setSaveloc3("location3");
        if (!"location3".equals(fileInfo.getSaveloc3())) {
            System.out.println("FAIL: saveloc3 getter/setter");
            allTestsPassed = false;
        }

        if (allTestsPassed) {
            System.out.println("SUCCESS: All getter/setter tests passed!");
        } else {
            System.out.println("FAILURE: Some tests failed.");
            System.exit(1);
        }
    }
}
