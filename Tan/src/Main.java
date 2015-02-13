import java.io.FileNotFoundException;
import java.text.ParseException;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
/**
 * Created by mhorowitzgelb on 2/13/2015.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        DataSource source = new DataSource("C:\\Users\\mhorowitzgelb\\Desktop\\lymph_train.arff");
        Instances instances = source.getDataSet();
        instances.setClassIndex(instances.numAttributes() -1);
        Tree tree = new Tree();
        double mutualInfoAB = tree.MutualInformation(instances, instances.attribute(4), instances.attribute(3));
        double mutualInfoBA = tree.MutualInformation(instances, instances.attribute(3), instances.attribute(4));

        System.out.println(mutualInfoAB);
        System.out.println(mutualInfoBA);
    }
}
