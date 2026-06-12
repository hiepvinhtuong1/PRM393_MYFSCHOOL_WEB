package vn.edu.fpt.myfptschool.grade.dto;

import vn.edu.fpt.myfptschool.grade.entity.GradeRecord;
import vn.edu.fpt.myfptschool.grade.entity.ScoreComponent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public record GradeSubjectResponse(
        Long classroomSubjectId,
        String subjectName,
        int subjectCoefficient,
        List<Double> regularScores,
        Double midtermScore,
        Double finalScore,
        Double subjectAverage,
        String status
) {
    public static GradeSubjectResponse from(Long csId, String subjectName, int coefficient,
                                            List<GradeRecord> records) {
        List<Double> regularScores = records.stream()
                .filter(gr -> gr.getComponent().getCode().startsWith("TX"))
                .sorted((a, b) -> a.getComponent().getDisplayOrder()
                        .compareTo(b.getComponent().getDisplayOrder()))
                .map(gr -> gr.getScore() != null ? gr.getScore().doubleValue() : null)
                .toList();

        Double midterm = records.stream()
                .filter(gr -> "DGKK".equals(gr.getComponent().getCode()))
                .findFirst()
                .map(gr -> gr.getScore() != null ? gr.getScore().doubleValue() : null)
                .orElse(null);

        Double finalScore = records.stream()
                .filter(gr -> "DCK".equals(gr.getComponent().getCode()))
                .findFirst()
                .map(gr -> gr.getScore() != null ? gr.getScore().doubleValue() : null)
                .orElse(null);

        Double avg = computeAverage(regularScores, midterm, finalScore);
        String status = computeStatus(finalScore, avg);

        return new GradeSubjectResponse(csId, subjectName, coefficient,
                regularScores, midterm, finalScore, avg, status);
    }

    // DTBm = (∑TX×1 + DGKK×2 + DCK×3) / (n_TX + 2 + 3)
    private static Double computeAverage(List<Double> regular, Double midterm, Double finalScore) {
        if (midterm == null || finalScore == null) return null;
        double sumTx = regular.stream().filter(s -> s != null)
                .mapToDouble(Double::doubleValue).sum();
        int nTx = (int) regular.stream().filter(s -> s != null).count();
        double total = sumTx + midterm * 2 + finalScore * 3;
        int count = nTx + 2 + 3;
        return BigDecimal.valueOf(total / count)
                .setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private static String computeStatus(Double finalScore, Double avg) {
        if (finalScore == null) return "inProgress";
        if (avg != null && avg >= 5.0) return "passed";
        return "warning";
    }
}
