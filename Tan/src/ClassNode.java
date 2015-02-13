import weka.core.Attribute;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mhorowitzgelb on 2/13/2015.
 */
public class ClassNode extends Node{
    public double nonConditionalProbability;

    public List<NonClassNode> children;


    public ClassNode(Attribute attribute){
        this.attribute = attribute;
        children = new ArrayList<NonClassNode>();
    }

    //public List<>

    @Override
    public double GetProbability(Instances instances) {
        return 0;
    }
}
