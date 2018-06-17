import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;
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

        INDArray sequence1 = Nd4j.create(new float[] {0.0f, 1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 9.0f});
        INDArray subSequence = sequence1.get(NDArrayIndex.interval(0, 1), NDArrayIndex.interval(2, 5));
        System.out.println(subSequence);

        INDArray sequence2 = Nd4j.create(new float[] {10.0f, 11.0f, 12.0f, 13.0f, 14.0f, 15.0f, 16.0f, 17.0f, 18.0f, 19.0f});

        INDArray sequence3 = Nd4j.create(1, sequence1.size(1) + sequence2.size(1));
        sequence3.put(new INDArrayIndex[] {NDArrayIndex.interval(0, 1), NDArrayIndex.interval(0, 10)}, sequence1);
        sequence3.put(new INDArrayIndex[] {NDArrayIndex.interval(0, 1), NDArrayIndex.interval(10, 20)}, sequence2);
        System.out.println(sequence3);
    }
}
