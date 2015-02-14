import weka.core.Attribute;
import weka.core.Instance;

import java.util.Map;

/**
 * Created by mhorowitzgelb on 2/13/2015.
 */
public class ConditionalProbability {
    public Attribute attribute;
    public Map<Attribute, String> valueMap;
    private double[] probabilites;

    public ConditionalProbability(Map<Attribute, String> valueMap, Attribute attribute , double[]probabilities) throws Exception {
        if(probabilities.length != attribute.numValues() -1)
            throw new Exception("Incorrect number of probabilities for this ConditionalProbability");
        this.attribute = attribute;
        this.valueMap = valueMap;
        this.probabilites = probabilities;
    }

    public boolean Matches(Instance instance, boolean ignoreClassAttribute){
        for(Attribute attribute : valueMap.keySet()){
            if(ignoreClassAttribute && instance.classAttribute().equals(attribute))
                continue;
            if(!instance.stringValue(attribute).equals(valueMap.get(attribute)))
                return false;
        }
        return true;
    }



    public double GetProbability(String attributeValue) throws Exception {
        double probability = 1;
        for(int i = 0; i < probabilites.length; i ++){
            probability -= probabilites[i];
            if(attribute.value(i).equals(attributeValue))
                return probabilites[i];
        }
        if(attribute.value(attribute.numValues() -1).equals(attributeValue)) {
            return probability;
        }
        throw new Exception("Attribute value " + attributeValue + " is not a valid value.");
    }

}
