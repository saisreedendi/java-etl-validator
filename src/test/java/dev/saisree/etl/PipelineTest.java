package dev.saisree.etl;

import java.util.List;

public class PipelineTest {
    public static void main(String[] a) {
        List<String> in = List.of("txn_id,customer_id,amount,currency,date",
                "T1,C1,50.00,USD,2026-01-01",
                "T1,C1,50.00,USD,2026-01-01",
                "T2,C1,20000,USD,2026-01-01",
                "T3,,5,USD,2026-01-01",
                "T4,C1,x,USD,2026-01-01");
        Pipeline.Result r = Pipeline.run(in);
        boolean ok = r.total() == 5 && r.clean().size() == 1 && r.flagged().size() == 1 && r.rejected().size() == 3;
        System.out.println(ok ? "ALL TESTS PASSED" : "FAILED " + r);
        if (!ok) System.exit(1);
    }
}
