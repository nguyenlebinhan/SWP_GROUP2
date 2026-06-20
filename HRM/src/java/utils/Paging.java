package utils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

public final class Paging {

    public static final int PAGE_SIZE = 10;

    private Paging() {
    }

    public static <T> List<T> page(HttpServletRequest request, List<T> items) {
        List<T> list = (items != null) ? items : new ArrayList<>();
        int totalItems = list.size();
        int totalPages = Math.max(1, (int) Math.ceil(totalItems / (double) PAGE_SIZE));
        int page = intParam(request, "page", 1);
        if (page < 1) {
            page = 1;
        }
        if (page > totalPages) {
            page = totalPages;
        }
        int from = (page - 1) * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, totalItems);
        List<T> pageItems = (from < to)
                ? new ArrayList<>(list.subList(from, to))
                : new ArrayList<>();

        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalItems", totalItems);
        return pageItems;
    }

    private static int intParam(HttpServletRequest request, String name, int def) {
        String raw = request.getParameter(name);
        if (raw != null && !raw.trim().isEmpty()) {
            try {
                return Integer.parseInt(raw.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return def;
    }
}
