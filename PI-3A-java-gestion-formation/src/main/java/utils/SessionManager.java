package utils;

public class SessionManager {

    private static Integer currentEmployeId = null;

    public static Integer getCurrentEmployeId() {
        return currentEmployeId;
    }

    public static void setCurrentEmployeId(Integer id) {
        currentEmployeId = id;
    }

    public static boolean isEmployeSelected() {
        return currentEmployeId != null;
    }

    public static void clear() {
        currentEmployeId = null;
    }
}

