package com.hsf_project.util;

public class VietnameseUtils {

    private VietnameseUtils() {}

    private static final char[] SOURCE = {
        'à', 'á', 'ả', 'ã', 'ạ', 'ă', 'ắ', 'ằ', 'ẵ', 'ặ',
        'â', 'ấ', 'ầ', 'ẫ', 'ậ',
        'đ',
        'è', 'é', 'ẻ', 'ẽ', 'ẹ', 'ê', 'ế', 'ề', 'ễ', 'ệ',
        'ì', 'í', 'ỉ', 'ĩ', 'ị',
        'ò', 'ó', 'ỏ', 'õ', 'ọ', 'ô', 'ố', 'ồ', 'ỗ', 'ộ',
        'ơ', 'ớ', 'ờ', 'ỡ', 'ợ',
        'ù', 'ú', 'ủ', 'ũ', 'ụ', 'ư', 'ứ', 'ừ', 'ữ', 'ự',
        'ỳ', 'ý', 'ỷ', 'ỹ', 'ỵ'
    };

    private static final char[] TARGET = {
        'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a',
        'a', 'a', 'a', 'a', 'a',
        'd',
        'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e',
        'i', 'i', 'i', 'i', 'i',
        'o', 'o', 'o', 'o', 'o', 'o', 'o', 'o', 'o', 'o',
        'o', 'o', 'o', 'o', 'o',
        'u', 'u', 'u', 'u', 'u', 'u', 'u', 'u', 'u', 'u',
        'y', 'y', 'y', 'y', 'y'
    };

    public static String removeDiacritics(String s) {
        if (s == null) return null;
        s = s.toLowerCase().trim();
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int idx = indexOf(SOURCE, c);
            if (idx >= 0) {
                sb.append(TARGET[idx]);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static int indexOf(char[] arr, char c) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == c) return i;
        }
        return -1;
    }
}
