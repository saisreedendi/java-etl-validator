package dev.saisree.etl;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

/**
 * Small ETL pipeline: Extract rows from a CSV, Transform (validate, remove duplicates, flag large
 * amounts), Load clean rows and rejected rows (with reasons) to output files.
 */
public class Pipeline {
    static final Set<String> CURRENCIES = Set.of("USD", "EUR", "GBP", "INR");
    static final BigDecimal REVIEW_LIMIT = new BigDecimal("10000");

    public record Result(List<String> clean, List<String> flagged, List<String> rejected, int total) {}

    public static void main(String[] args) throws IOException {
        Path in = Path.of(args.length > 0 ? args[0] : "data/transactions.csv");
        Path outDir = Path.of(args.length > 1 ? args[1] : "output");
        Result r = run(Files.readAllLines(in));
        Files.createDirectories(outDir);
        Files.write(outDir.resolve("clean.csv"), r.clean());
        Files.write(outDir.resolve("flagged_for_review.csv"), r.flagged());
        Files.write(outDir.resolve("rejected.csv"), r.rejected());
        System.out.printf("Read %d rows: %d clean, %d flagged for review, %d rejected%n",
                r.total(), r.clean().size(), r.flagged().size(), r.rejected().size());
    }

    public static Result run(List<String> lines) {
        List<String> clean = new ArrayList<>(), flagged = new ArrayList<>(), rejected = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        int total = 0;
        for (String line : lines.subList(1, lines.size())) {      // skip header
            if (line.isBlank()) continue;
            total++;
            try {
                String[] c = line.split(",", -1);
                validate(c);
                if (!seen.add(c[0])) throw new IllegalArgumentException("duplicate txn_id");
                if (new BigDecimal(c[2]).compareTo(REVIEW_LIMIT) > 0) flagged.add(line);
                else clean.add(line);
            } catch (IllegalArgumentException e) {            // one bad row never stops the run
                rejected.add(line + "," + e.getMessage());
            }
        }
        return new Result(clean, flagged, rejected, total);
    }

    static void validate(String[] c) {
        if (c.length != 5) throw new IllegalArgumentException("wrong number of columns");
        if (c[0].isBlank()) throw new IllegalArgumentException("missing txn_id");
        if (c[1].isBlank()) throw new IllegalArgumentException("missing customer_id");
        try {
            if (new BigDecimal(c[2]).signum() <= 0) throw new IllegalArgumentException("amount must be positive");
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("amount is not a number");
        }
        if (!CURRENCIES.contains(c[3])) throw new IllegalArgumentException("unknown currency");
        try {
            LocalDate.parse(c[4]);
        } catch (java.time.format.DateTimeParseException e) {
            throw new IllegalArgumentException("invalid date");
        }
    }
}
