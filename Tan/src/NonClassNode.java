import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;

/**
 * Created by mhorowitzgelb on 2/13/2015.
 */
public class NonClassNode extends Node {
    List<Attribute> conditionalAttributes;
    List<ConditionalProbability> conditionalProbabilities;
    List<Node> parents;
    List<Node> children;

    public NonClassNode(Attribute attribute){
        conditionalAttributes = new ArrayList<Attribute>();
        this.attribute = attribute;
        conditionalProbabilities = new ArrayList<ConditionalProbability>();
        parents = new ArrayList<Node>();
        children = new ArrayList<Node>();
    }

    public void SetConditionalProbabilities(Instances instances){
        for(Node node : parents){
            conditionalAttributes.add(node.attribute);
        }
        RecursiveSetProbabilities(instances, new HashMap<Attribute, String>(),0);
    }

    private void RecursiveSetProbabilities(Instances instances, Map<Attribute, String> valueMap,int i){
        if(i >= parents.size()){
            double[] probabilities = new double[attribute.numValues() - 1];
            double total = Tree.Count(instances, valueMap);
            for(int valueIndex = 0; valueIndex < probabilities.length; valueIndex ++){
                probabilities[valueIndex] = (Tree.CountAttributeValue(instances, valueMap, attribute, attribute.value(valueIndex)) + 1) / (total + attribute.numValues());
            }
            try {
                conditionalProbabilities.add(new ConditionalProbability(valueMap,attribute, probabilities));
            }catch (Exception e){
                System.out.println(e.getMessage());
                System.exit(-1);
            }
            return;
        }
        Attribute nextSplit = parents.get(i).attribute;
        for(int valueIndex = 0; valueIndex < nextSplit.numValues(); valueIndex ++){
            HashMap<Attribute, String> newValueMap = new HashMap<Attribute, String>(valueMap);
            newValueMap.put(nextSplit, nextSplit.value(valueIndex));
            RecursiveSetProbabilities(instances, newValueMap, i + 1);
        }
    }


    @Override
    public double GetProbability(Instances instances) {
        return 0;
    }
}
