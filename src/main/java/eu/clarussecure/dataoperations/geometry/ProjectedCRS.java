package eu.clarussecure.dataoperations.geometry;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProjectedCRS {

    public static class Axis {
        private final int order;
        private final String name;
        private final String abbreviation;
        private final String unit;
        private final String orientation;
        private final double min;
        private final double max;

        private Axis(int order, String name, String abbreviation, String unit, String orientation, double min,
                double max) {
            this.order = order;
            this.name = name;
            this.abbreviation = abbreviation;
            this.unit = unit;
            this.orientation = orientation;
            this.min = min;
            this.max = max;
        }

        public int getOrder() {
            return order;
        }

        public String getName() {
            return name;
        }

        public String getAbbreviation() {
            return abbreviation;
        }

        public String getUnit() {
            return unit;
        }

        public String getOrientation() {
            return orientation;
        }

        public double getMin() {
            return min;
        }

        public double getMax() {
            return max;
        }
    }

    private final int id;
    private final String name;
    private final List<Axis> axes;
    private final Map<String, Integer> abbreviationToOrder;

    private ProjectedCRS(int id, String name, List<Axis> axes) {
        super();
        this.id = id;
        this.name = name;
        this.axes = axes;
        this.abbreviationToOrder = this.axes.stream()
                .collect(Collectors.toMap(axis -> axis.getAbbreviation().toLowerCase(), Axis::getOrder));
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Axis> getAxes() {
        return axes;
    }

    public Axis getAxis(int order) {
        return order <= axes.size() ? axes.get(order - 1) : null;
    }

    public Axis getAxis(String abbreviation) {
        Integer order = abbreviationToOrder.get(abbreviation.toLowerCase());
        return order != null ? getAxis(order) : null;
    }

    private static final Map<Integer, ProjectedCRS> ALL_PROJECTED_CRS;

    static {
        List<ProjectedCRS> allProjectedCRS = Collections.emptyList();
        Properties properties = new Properties();
        try (InputStream inputStream = ProjectedCRS.class.getResourceAsStream("all-projected-crs.properties")) {
            properties.load(inputStream);
            allProjectedCRS = properties.keySet().stream().map(String.class::cast)
                    .map(k -> k.substring(0, k.indexOf('.')))
                    .distinct().map(
                            Integer::valueOf)
                    .map(srid -> new ProjectedCRS(srid,
                            properties
                                    .getProperty(
                                            String.format("%s.name", srid)),
                            properties.keySet().stream().map(String.class::cast)
                                    .filter(k -> k.startsWith(String.format("%s.axes.", srid)))
                                    .map(k -> k.substring(k.lastIndexOf('.') + 1)).distinct().map(Integer::valueOf)
                                    .sorted()
                                    .map(order -> order + ","
                                            + properties.getProperty(String.format("%s.axes.%d", srid, order)))
                                    .map(axe -> axe.split(","))
                                    .map(tk -> new ProjectedCRS.Axis(Integer.parseInt(tk[0]), tk[1], tk[2], tk[3],
                                            tk[4], Double.parseDouble(tk[5]), Double.parseDouble(tk[6])))
                                    .collect(Collectors.toList())))
                    .collect(Collectors.toList());
        } catch (IOException e) {
        }
        ALL_PROJECTED_CRS = allProjectedCRS.stream()
                .collect(Collectors.toMap(ProjectedCRS::getId, Function.identity()));
    }

    public static final ProjectedCRS resolve(int srid) {
        return ALL_PROJECTED_CRS.get(srid);
    }
}
