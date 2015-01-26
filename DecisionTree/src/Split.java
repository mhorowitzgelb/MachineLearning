import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
/**
 * Created by maxhorowitz on 1/24/15.
 */
public class Split {
    public static int m;
    public String s_val;
    public List<Split> children;
    public List<String[]> instancesRemaining;
    public ARFFParser.Attribute attributeToSplit;
    public double threshold;
    public String classVal;

    public void ChooseBestSplit(List<ARFFParser.Attribute> attributes){
        if(instancesRemaining.size() < m || attributes.size() ==0){
            int[] classCount = countClasses(instancesRemaining);
            if(classCount[1] > classCount[0]){
                classVal = "positive";
            }
            else{
                classVal = "negative";
            }
            return;
        }


        double bestInfoGain = Double.NEGATIVE_INFINITY;
        ARFFParser.Attribute bestAttribute = null;
        double bestThreshold = 0;
        for(ARFFParser.Attribute attribute : attributes){
            if(attribute.name.equals("class")){
                break;
            }
            if(attribute.type == ARFFParser.Type.Nominal){
                double infoGain = InfoGain(attribute);
                //System.out.println("Info gain for " + attribute.name + " is " + infoGain);
                if(infoGain > bestInfoGain){
                    bestInfoGain = infoGain;
                    bestAttribute = attribute;
                }
            }
            else{
                Collections.sort(instancesRemaining, new InstanceComparator(attribute.index));
                double infoGain;
                for(int i = 0; i < instancesRemaining.size() - 1; i ++) {
                    double instanceA = Double.parseDouble(instancesRemaining.get(i)[attribute.index]);
                    double instanceB = Double.parseDouble(instancesRemaining.get(i + 1)[attribute.index]);
                    if(instanceA == instanceB)
                        continue;
                    double threshold = (instanceA + instanceB) / 2.0;
                    infoGain = InfoGain(attribute, threshold);
                    //System.out.println("Info gain for " + attribute.name + "at threshold "+ threshold + " is " + infoGain);

                    if(infoGain > bestInfoGain){
                        bestAttribute = attribute;
                        bestInfoGain = infoGain;
                        bestThreshold = threshold;
                    }
                }
            }
        }

        if(bestInfoGain <= 0){
            int[] classCount = countClasses(instancesRemaining);
            if(classCount[1] > classCount[0]){
                classVal = "positive";
            }
            else{
                classVal = "negative";
            }
            return;
        }
        if(bestAttribute == null){
            System.out.println("what the fuck");
        }
        this.threshold = bestThreshold;
        this.attributeToSplit = bestAttribute;
        List<ARFFParser.Attribute> childAttributes = new ArrayList<ARFFParser.Attribute>(attributes);
        if(bestAttribute.type != ARFFParser.Type.Real)
            childAttributes.remove(bestAttribute);
        children = new ArrayList<Split>();
        if(attributeToSplit.type == ARFFParser.Type.Nominal){
            for(String val : attributeToSplit.nominalValues){
                List<String[]> childInstances = new ArrayList<String[]>();
                for(int i = 0; i < instancesRemaining.size(); i ++){
                    if(instancesRemaining.get(i)[attributeToSplit.index].equals(val)){
                        childInstances.add(instancesRemaining.get(i));
                    }
                }


                Split child =  new Split();
                if(childInstances.size() == 0){
                    child.s_val = val;
                    int[] classCount = countClasses(instancesRemaining);
                    if(classCount[1] > classCount[0]){
                        child.classVal = "positive";
                    }
                    else{
                        child.classVal = "negative";
                    }
                }
                else {
                    child.s_val = val;
                    child.instancesRemaining = childInstances;
                    child.ChooseBestSplit(childAttributes);
                }
                children.add(child);
            }
        }
        else if(attributeToSplit.type == ARFFParser.Type.Real){
            List<String[]> ltEqThreshold = new ArrayList<String[]>();
            List<String[]> greaterThreshold = new ArrayList<String[]>();
            for(String[] instance : instancesRemaining){
                double val = Double.parseDouble(instance[bestAttribute.index]);
                if(val > threshold){
                    greaterThreshold.add(instance);
                }
                else{
                    ltEqThreshold.add(instance);
                }
            }
            Split ltEqSplit = new Split();
            if(ltEqThreshold.size() == 0){
                int[] classCount = countClasses(instancesRemaining);
                if(classCount[1] > classCount[0]){
                    ltEqSplit.classVal = "positive";
                }
                else{
                    ltEqSplit.classVal = "negative";
                }
            }
            else{
                ltEqSplit.instancesRemaining = ltEqThreshold;
                ltEqSplit.ChooseBestSplit(childAttributes);
            }
            children.add(ltEqSplit);
            Split greaterSplit = new Split();
            if(greaterThreshold.size() == 0){
                int[] classCount = countClasses(instancesRemaining);
                if(classCount[1] > classCount[0]){
                    greaterSplit.classVal = "positive";
                }
                else{
                    greaterSplit.classVal = "negative";
                }
            }
            else{
                greaterSplit.instancesRemaining = greaterThreshold;
                greaterSplit.ChooseBestSplit(childAttributes);
            }
            children.add(greaterSplit);
        }
        else{
            System.out.println("What the fuck");
        }
    }

    public int[] countClasses(List<String[]> instances){
        int[] total = {0,0};
        for(String[] instance : instances){
            if(instance[instance.length -1].equals("negative"))
                total[0] ++;
            else
                total[1] ++;
        }
        return total;
    }

    public double InfoGain(ARFFParser.Attribute attribute){
        double entropy = entropy(instancesRemaining);
        double conditionalEntropy = 0;
        for(String val : attribute.nominalValues){
            List<String[]> instancesWithVal = new ArrayList<String[]>();
            for(String[] instance : instancesRemaining){
                if(instance[attribute.index].equals(val)){
                    instancesWithVal.add(instance);
                }
            }
            double pVal = instancesWithVal.size() / (double) instancesRemaining.size();
            conditionalEntropy += pVal* entropy(instancesWithVal);
        }
        return entropy - conditionalEntropy;
    }

    public double entropy(List<String[]> instances){
        int[] classCount = countClasses(instances);
        if(classCount[1] == 0 || classCount[0] == 0){
            return 0;
        }
        double pNegative = classCount[0] / (double)instances.size();
        double pPositive = classCount[1] / (double)instances.size();
        return -(pNegative* log2(pNegative)) - (pPositive * log2(pPositive));
    }


    public static final double LOGOF2 = 0.693147180559945309417232121458176568075500134360255254120680;
    public double log2(double a){
        return (double) (Math.log(a) / LOGOF2);
    }

    public double InfoGain(ARFFParser.Attribute attribute, double threshold){
        double entropy = entropy(instancesRemaining);
        double conditionalEntropy = 0;
        List<String[]> ltEqThreshold = new ArrayList<String[]>();
        List<String[]> greaterThreshold = new ArrayList<String[]>();
        for(String[] instance : instancesRemaining){
            double val = Double.parseDouble(instance[attribute.index]);
            if(val > threshold){
                greaterThreshold.add(instance);
            }
            else{
                ltEqThreshold.add(instance);
            }
        }
        double pLtEq = ltEqThreshold.size() / (double) instancesRemaining.size();
        double pGreater = greaterThreshold.size() / (double) instancesRemaining.size();
        conditionalEntropy += pLtEq * entropy(ltEqThreshold);
        conditionalEntropy += pGreater * entropy(greaterThreshold);
        return entropy - conditionalEntropy;
    }

    public String classify(String[] instance){
        if(classVal != null){
            return classVal;
        }
        if(attributeToSplit == null){
            System.out.println("what the fucl");
        }
        if(attributeToSplit.type == ARFFParser.Type.Nominal){
            for(Split child : children){
                if(child.s_val == null){
                    System.out.println("What the fuck");
                }
                if(child.s_val.equals(instance[attributeToSplit.index])){
                    return child.classify(instance);
                }
            }
            return null;
        }
        else{
            double val = Double.parseDouble(instance[attributeToSplit.index]);
            if(val > threshold){
                return children.get(1).classify(instance);
            }
            else{
                return  children.get(0).classify(instance);
            }
        }
    }

    public void Print(ARFFParser.Attribute attr, boolean greater ,double threshold, int tabs){
        for(int i = 0; i < tabs; i ++){
            System.out.print("|\t");
        }
        if(attr.type == ARFFParser.Type.Real){
            System.out.print(attr.name+ " ");
            if(greater){
                System.out.print("> " + threshold+" ");
            }
            else{
                System.out.print("<= "+ threshold+" ");
            }
        }
        else{
            System.out.print(attr.name+" = "+ s_val);
        }
        if(classVal != null){
            System.out.println(": "+ classVal);
        }
        else {
            System.out.println();
            if(attributeToSplit.type == ARFFParser.Type.Nominal) {
                for (Split child : children){
                    child.Print(attributeToSplit, false, 0, tabs + 1);
                }
            }
            else{
                children.get(0).Print(attributeToSplit,false,this.threshold,tabs+1);
                children.get(1).Print(attributeToSplit, true,this.threshold,tabs+1);
            }
        }
    }

    public static class InstanceComparator implements Comparator<String[]>{

        private int index;
        public InstanceComparator(int index){
            this.index = index;
        }
        @Override
        public int compare(String[] o1, String[] o2) {
            double a = Double.parseDouble(o1[index]);
            double b = Double.parseDouble(o2[index]);
            if(a > b)
                return 1;
            else if(a < b)
                return -1;
            else return 0;
        }
    }
}
