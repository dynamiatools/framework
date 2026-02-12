package mybookstore.domain.enums;

import tools.dynamia.commons.InstanceName;

import java.util.Random;

public enum StockStatus {
    IN_STOCK("Available"), OUT_STOCK("Out stock"), PREORDER("Preorder");


    private String description;

    StockStatus(String description) {
        this.description = description;
    }

    public static StockStatus random() {
        int index = new Random().nextInt(StockStatus.values().length);
        return values()[index];
    }

    @InstanceName
    public String getDescription() {
        return description;
    }
}
