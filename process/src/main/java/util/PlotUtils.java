package util;

import com.github.sh0nk.matplotlib4j.Plot;
import com.github.sh0nk.matplotlib4j.PythonConfig;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class PlotUtils {
    @SneakyThrows
    public void draw(List<Pair<Double, Double>> points, String xLabel, String yLabel) {
        List<Pair<? extends Number, ? extends Number>> sorted = points.stream()
                .sorted(Comparator.comparing(Pair::getLeft))
                .collect(Collectors.toList());
        var x = sorted.stream()
                .map(Pair::getLeft)
                .collect(Collectors.toList());
        var y = sorted.stream()
                .map(Pair::getRight)
                .collect(Collectors.toList());

        Plot plt = Plot.create(PythonConfig.pythonBinPathConfig("/home/kamotora/anaconda3/bin/python"));
        plt.plot()
                .add(x, y, "-ok");
        plt.xlabel(xLabel);
        plt.ylabel(yLabel);
        plt.legend();
        plt.show();
    }
}
