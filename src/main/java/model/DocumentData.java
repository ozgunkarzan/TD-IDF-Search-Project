package model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DocumentData implements Serializable {
    private Map<String, Double> termToFrequency= new HashMap<>();

    public void putTermFrequency(String term, double freqeucy){
        termToFrequency.put(term,freqeucy);
    }

    public double getFrequency(String term){
        return termToFrequency.get(term);
    }
}
