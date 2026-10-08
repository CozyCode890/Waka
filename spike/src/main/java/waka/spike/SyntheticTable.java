package waka.spike;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;

/**
 * Probe B and Probe C, data half: tables that hold data without reading a file and without
 * touching Weka, so a slow ARFF parser can never be mistaken for a slow TableView.
 *
 * <p>Every cell value is a reference into one small pool of strings built once at class load.
 * Reading a cell is therefore an array index with no parsing, no formatting and no allocation on
 * the frame path. That matters at the top of the column sweep: 10000 rows by 2000 columns is
 * twenty million cells, and materialising a distinct string for each would measure the garbage
 * collector rather than the table. Whatever the frame times say here, they say it about TableView.
 *
 * <p>A data row is represented by its own index. The row object carries no state because the
 * real application's row object will not either: it will read through to Weka's Instances.
 */
public final class SyntheticTable {

    /** Pool size. A power of two, so picking a value can mask instead of divide. */
    private static final int POOL_SIZE = 4096;

    private static final String[] VALUE_POOL = buildValuePool();

    private static final String[] ATTRIBUTE_TYPES = { "numeric", "nominal", "string", "date" };

    private SyntheticTable() {
    }

    // ---- data rows: one column per attribute, which is what a data sheet has to do ----

    public static ObservableList<Integer> rowIndexes(int rowCount) {
        List<Integer> indexes = new ArrayList<>(rowCount);
        for (int index = 0; index < rowCount; index++) {
            indexes.add(index);
        }
        return FXCollections.observableArrayList(indexes);
    }

    public static List<TableColumn<Integer, String>> dataColumns(int columnCount) {
        List<TableColumn<Integer, String>> columns = new ArrayList<>(columnCount);
        for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
            final int thisColumn = columnIndex;
            TableColumn<Integer, String> column = new TableColumn<>("attribute " + (columnIndex + 1));
            column.setPrefWidth(96);
            // Sorting a hundred thousand rows is not what this probe measures.
            column.setSortable(false);
            column.setCellValueFactory(cell ->
                    new ReadOnlyObjectWrapper<>(cellValue(cell.getValue(), thisColumn)));
            columns.add(column);
        }
        return columns;
    }

    public static String cellValue(int rowIndex, int columnIndex) {
        return VALUE_POOL[(rowIndex * 31 + columnIndex * 17) & (POOL_SIZE - 1)];
    }

    // ---- attribute rows: the shape the application actually ships (DESIGN.md) ----

    /** One row of the attribute list. DESIGN.md: attributes are rows, never columns. */
    public record AttributeRow(int number, String name, String type,
                               int missingCount, int distinctCount) {
    }

    public static ObservableList<AttributeRow> attributeRows(int attributeCount) {
        Random random = new Random(20261008L);
        List<AttributeRow> rows = new ArrayList<>(attributeCount);
        for (int index = 0; index < attributeCount; index++) {
            rows.add(new AttributeRow(
                    index + 1,
                    "attribute_" + (index + 1),
                    ATTRIBUTE_TYPES[index % ATTRIBUTE_TYPES.length],
                    random.nextInt(50),
                    1 + random.nextInt(200)));
        }
        return FXCollections.observableArrayList(rows);
    }

    public static List<TableColumn<AttributeRow, String>> attributeColumns() {
        List<TableColumn<AttributeRow, String>> columns = new ArrayList<>(5);
        columns.add(attributeColumn("No", 52, row -> String.valueOf(row.number())));
        columns.add(attributeColumn("Name", 240, AttributeRow::name));
        columns.add(attributeColumn("Type", 96, AttributeRow::type));
        columns.add(attributeColumn("Missing", 96, row -> String.valueOf(row.missingCount())));
        columns.add(attributeColumn("Distinct", 96, row -> String.valueOf(row.distinctCount())));
        return columns;
    }

    private interface AttributeText {
        String of(AttributeRow row);
    }

    private static TableColumn<AttributeRow, String> attributeColumn(String heading, double width,
                                                                     AttributeText text) {
        TableColumn<AttributeRow, String> column = new TableColumn<>(heading);
        column.setPrefWidth(width);
        column.setSortable(false);
        column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(text.of(cell.getValue())));
        return column;
    }

    // ---- the pool ----

    private static String[] buildValuePool() {
        Random random = new Random(20261008L);
        String[] nominalValues = { "yes", "no", "maybe", "unknown", "setosa", "versicolor",
                                   "virginica", "low", "medium", "high" };
        String[] pool = new String[POOL_SIZE];
        for (int index = 0; index < POOL_SIZE; index++) {
            int shape = index % 10;
            if (shape == 0) {
                // Weka's missing-value marker. A real dataset has holes in it.
                pool[index] = "?";
            } else if (shape < 4) {
                pool[index] = nominalValues[random.nextInt(nominalValues.length)];
            } else {
                pool[index] = String.format("%.4f", random.nextDouble() * 1000.0);
            }
        }
        return pool;
    }
}
