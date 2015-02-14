import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            double probability =
            conditionalProbabilities.add(new ConditionalProbability(valueMap,));
        }

    }


    @Override
    public double GetProbability(Instances instances) {
        return 0;
    }
}
