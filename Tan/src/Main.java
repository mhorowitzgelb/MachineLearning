import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
/**
 * Created by mhorowitzgelb on 2/13/2015.
 */
public class Main {
    public static void main(String[] args) throws Exception {

        if(args.length != 3){
            System.out.println("Please input of form bayes <train-set-file> <test-set-file> <n|t>");
            System.exit(0);
        }

        DataSource source = new DataSource(args[0]);
        Instances instances = source.getDataSet();
        instances.setClassIndex(instances.numAttributes() -1);

        boolean isTan = false;
        if(args[2].equals("n"))
            isTan = false;
        else if(args[2].equals("t"))
            isTan = true;
        else{
            System.out.println("Pleas input n for naive bayes or t for tan");
            System.exit(0);
        }


        Tree tree = new Tree(instances,isTan);

        for(NonClassNode node : tree.classNode.children){
            System.out.print(node.attribute.name());
            for(int i = 1; i < node.parents.size(); i ++){
                Node parent = node.parents.get(i);
                System.out.print(" " + parent.attribute.name());
            }
            System.out.print(" " + node.parents.get(0).attribute.name());
            System.out.println();
        }

        System.out.println();
        DataSource testSource = new DataSource(args[1]);
        Instances testInstances = testSource.getDataSet();
        testInstances.setClassIndex(testInstances.numAttributes() -1);
        int correctlyClassified = 0;
        for(Instance instance : testInstances){
            double probabilityYouGonnaDie = tree.Classify(instance);
            if(probabilityYouGonnaDie > 0.5){
                System.out.print(instance.classAttribute().value(0)+ " ");
                System.out.print(instance.stringValue(instance.classAttribute()));
                System.out.println(" " + probabilityYouGonnaDie);
                if(instance.stringValue(instance.classAttribute()).equals(instance.classAttribute().value(0)))
                    correctlyClassified ++;
            }
            else{
                System.out.print(instance.classAttribute().value(1) + " ");
                System.out.print(instance.stringValue(instance.classAttribute()));
                System.out.println(" " + (1 -probabilityYouGonnaDie));
                if(instance.stringValue(instance.classAttribute()).equals(instance.classAttribute().value(1)))
                    correctlyClassified ++;
            }

        }

        System.out.println("\n"+correctlyClassified);
    }
}
