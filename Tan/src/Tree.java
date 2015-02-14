import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;

/**
 * Created by mhorowitzgelb on 2/13/2015.
 */
public class Tree {


    public final double LOG_OF_2 = Math.log(2);
    public ClassNode classNode;
    public Instances learningInstances;

    public Tree(Instances learningInstances, boolean TAN){
        this.learningInstances = learningInstances;

        this.classNode = new ClassNode(learningInstances.classAttribute());
        for(Enumeration<Attribute> e = learningInstances.enumerateAttributes(); e.hasMoreElements();){
            Attribute attribute = e.nextElement();
            if(!learningInstances.classAttribute().equals(attribute)){
                NonClassNode node = new NonClassNode(attribute);
                node.parents.add(classNode);
                classNode.children.add(node);
            }
        }
        if(TAN)
            Prim();
        classNode.SetNonConditional(learningInstances);
        for(NonClassNode node : classNode.children){
            node.SetConditionalProbabilities(learningInstances);
        }
    }

    public double ConditionalMutalInformation(Instances instances, Attribute a, Attribute b){
        double sum = 0;
        for(int i = 0; i < a.numValues(); i ++){
            String aValue = a.value(i);
            for(int j = 0; j < b.numValues(); j ++){
                String bValue = b.value(j);
                for(int k = 0; k < instances.classAttribute().numValues(); k ++){
                    Attribute classAttribute = instances.classAttribute();
                    String cValue = instances.classAttribute().value(k);

                    HashMap<Attribute, String> abcMap = new HashMap<Attribute, String>();
                    abcMap.put(a, aValue);
                    abcMap.put(b, bValue);
                    abcMap.put(classAttribute, cValue);

                    HashMap<Attribute , String> cMap = new HashMap<Attribute, String>();
                    cMap.put(classAttribute, cValue);

                    HashMap<Attribute, String> acMap = new HashMap<Attribute, String>();
                    acMap.put(classAttribute, cValue);
                    acMap.put(a,aValue);

                    HashMap<Attribute, String> bcMap = new HashMap<Attribute, String>();
                    bcMap.put(b,bValue);
                    bcMap.put(classAttribute, cValue);

                    double pABC = (Count(instances, abcMap) + 1) / (instances.numInstances()  + a.numValues() * b.numValues() * classAttribute.numValues());
                    double pABGivenC = (Count(instances, abcMap) + 1) / (Count(instances, cMap) + a.numValues()*b.numValues());
                    double pAGivenC = (Count(instances, acMap) + 1) / (Count(instances, cMap) + a.numValues());
                    double pBGivenC = (Count(instances, bcMap) + 1) / (Count(instances, cMap) + b.numValues());

                    if(pABC != 0){
                        sum += pABC * (Math.log(pABGivenC / (pAGivenC * pBGivenC)) / LOG_OF_2);
                    }
                }
            }
        }
        return sum;
    }

    public double MutualInformation(Instances instances, Attribute a, Attribute b){
        double sum = 0;
        for(int i = 0; i < a.numValues(); i ++){
            String aValue = a.value(i);
            for(int j = 0; j < b.numValues(); j ++){
                String bValue = b.value(j);
                HashMap<Attribute, String> abValueMap = new HashMap<Attribute, String>();
                abValueMap.put(a, aValue);
                abValueMap.put(b, bValue);
                double pAB = Count(instances, abValueMap) / instances.numInstances();

                HashMap<Attribute, String> aValueMap = new HashMap<Attribute, String>();
                aValueMap.put(a,aValue);
                double pA = Count(instances, aValueMap) / instances.numInstances();

                HashMap<Attribute, String> bValueMap = new HashMap<Attribute, String>();
                bValueMap.put(b, bValue);
                double pB = Count(instances, bValueMap) / instances.numInstances();


                if(pAB != 0)
                    sum += pAB * (Math.log(pAB/(pA*pB)) / LOG_OF_2);
            }
        }
        return sum;
    }


    public static double Count(Instances instances, Map<Attribute , String> valueMap){
        double count = 0;
        for(Instance instance : instances){
            boolean match = true;
            for(Attribute attribute : valueMap.keySet()){
                if(!instance.stringValue(attribute).equals(valueMap.get(attribute))){
                    match = false;
                    break;
                }
            }
            if(match){
                count ++;
            }
        }
        return count;
    }

    public static double CountFirstClassValue(Instances instances, Map<Attribute, String> valueMap){
        return CountAttributeValue(instances, valueMap, instances.classAttribute(), instances.classAttribute().value(0));
    }

    public static double CountAttributeValue(Instances instances, Map<Attribute, String> valueMap, Attribute attributeToCheck, String attributeValueToCheck){
        double count = 0;
        for(Instance instance : instances){
            if(!instance.stringValue(attributeToCheck).equals(attributeValueToCheck))
                continue;
            boolean match = true;
            for(Attribute attribute : valueMap.keySet()){
                if(!instance.stringValue(attribute).equals(valueMap.get(attribute)))
                {
                    match = false;
                    break;
                }
            }
            if(match)
                count ++;
        }
        return count;
    }

    public void Prim(){
        List<NonClassNode> vNew = new ArrayList<NonClassNode>();
        List<NonClassNode> v = new ArrayList<NonClassNode>(classNode.children);
        vNew.add(v.remove(0));
        while(v.size() > 0) {
            double maxMutualInfo = Double.NEGATIVE_INFINITY;
            NonClassNode maxEdgeA = null;
            NonClassNode maxEdgeB = null;
            for (NonClassNode aNode : vNew){
                for (NonClassNode bNode : v) {
                    double mutualInfo = ConditionalMutalInformation(learningInstances, aNode.attribute, bNode.attribute);
                    if(mutualInfo > maxMutualInfo){
                        maxMutualInfo = mutualInfo;
                        maxEdgeA = aNode;
                        maxEdgeB = bNode;
                    }
                    else if(mutualInfo == maxMutualInfo){
                        if(AttributeIndex(aNode.attribute) < AttributeIndex(maxEdgeA.attribute)){
                            maxMutualInfo = mutualInfo;
                            maxEdgeA = aNode;
                            maxEdgeB = bNode;
                        }
                        else if(aNode.attribute.equals(maxEdgeA.attribute) && AttributeIndex(bNode.attribute) < AttributeIndex(maxEdgeB.attribute)){
                            maxMutualInfo = mutualInfo;
                            maxEdgeA = aNode;
                            maxEdgeB = bNode;
                        }
                    }
                }
            }
            maxEdgeA.children.add(maxEdgeB);
            maxEdgeB.parents.add(maxEdgeA);
            v.remove(maxEdgeB);
            vNew.add(maxEdgeB);
        }

    }

    private int AttributeIndex( Attribute attribute){
        for(int i = 0; i < learningInstances.numAttributes(); i ++){
            if(learningInstances.attribute(i).equals(attribute))
                return i;
        }
        System.out.println("Invalid attribute for attribute index");
        System.exit(0);
        return 0;
    }

    public double Classify(Instance instance) throws Exception {
        double pC = classNode.nonConditionalProbability;
        double a = pC;
        double b = 1 -pC;
        for(NonClassNode node : classNode.children){
            for(ConditionalProbability cP : node.conditionalProbabilities){
                if(cP.Matches(instance,true)){
                    if(cP.valueMap.get(instance.classAttribute()).equals(instance.classAttribute().value(0)))
                        a *= cP.GetProbability(instance.stringValue(node.attribute));
                    else if(cP.valueMap.get(instance.classAttribute()).equals(instance.classAttribute().value(1)))
                        b *= cP.GetProbability(instance.stringValue(node.attribute));
                }
            }
        }
        return a / (a+b);
    }
}
