import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;

public class TestNd4j {
    @Test
    public void testMath() {
        INDArray layer = Nd4j.create(new int[] {1, 1}, new float[]{2.0f});
        Transforms.tanh(layer, false);
        System.out.println(layer);

        layer.muli(layer);
        System.out.println(layer);
        layer.rsubi(1.0);
        System.out.println(layer);
    }
}
