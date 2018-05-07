package edu.rylynn.datamining.core.associate;

import edu.rylynn.datamining.core.associate.common.ItemSet;

import java.util.*;

public class Eclat {
    List<String[]> itemData;
    private int minSupport;
    private int minConfidence;
    private Map<ItemSet, Integer> frequentItemSet;
    private Map<ItemSet, Set<Integer>> tidSets;
    private Map<String, Integer> itemIndex;

    public Eclat(int minSupport, int minConfidence, List<String> data) {
        this.minSupport = minSupport;
        this.minConfidence = minConfidence;
        this.itemIndex = new HashMap<>();
        this.tidSets = new HashMap<>();
        this.frequentItemSet = new HashMap<>();
        itemData = new ArrayList<>();

        for (String line : data) {
            itemData.add(line.split(","));
        }
    }

    public static void main(String[] args) {
        List<String> list = new ArrayList<String>();
        list.add("a,b,c,d");
        list.add("c,d,e,f");
        list.add("c,d,e,f");
        list.add("c,d,e,f");
        list.add("c,d,e,f");
        System.out.println(new Eclat(2, 2, list).generateFrequentItemSet());

    }

    public void generateTIDSet() {
        int index = 0;
        for (int i = 0; i < itemData.size(); i++) {
            String[] line = itemData.get(i);
            for (int j = 0; j < line.length; j++) {
                if (itemIndex.containsKey(line[j])) {
                    int thisIndex = itemIndex.get(line[j]);
                    int[] item = new int[1];
                    item[0] = thisIndex;
                    ItemSet thisItemSet = new ItemSet(1, item);
                    tidSets.get(thisItemSet).add(i);
                } else {
                    itemIndex.put(line[j], index++);
                    Set<Integer> set = new HashSet<>();
                    set.add(i);
                    int[] item = new int[1];
                    item[0] = index - 1;
                    ItemSet thisItemSet = new ItemSet(1, item);
                    tidSets.put(thisItemSet, set);

                }
            }
        }
        System.out.println(this.tidSets);
    }

    private ItemSet generateSuperSet(ItemSet cn1, ItemSet cn2) {
        int[] cn1Item = cn1.getItem();
        int[] cn2Item = cn2.getItem();
        Arrays.sort(cn1Item);
        Arrays.sort(cn2Item);
        int len1 = cn1Item.length;
        int len2 = cn2Item.length;
        int[] superSet = new int[len1 + 1];
        if (len1 == len2) {
            int flag = 1;
            for (int i = 0; i < len1 - 1; i++) {
                if (cn1Item[i] != cn2Item[i]) {
                    return null;
                } else {
                    superSet[i] = cn1Item[i];
                }
            }
            if (cn1Item[len1 - 1] != cn2Item[len1 - 1]) {
                if (cn1Item[len1 - 1] < cn2Item[len1 - 1]) {
                    superSet[len1 - 1] = cn1Item[len1 - 1];
                    superSet[len1] = cn2Item[len1 - 1];
                } else {
                    superSet[len1 - 1] = cn2Item[len1 - 1];
                    superSet[len1] = cn1Item[len1 - 1];
                }

                return new ItemSet(len1 + 1, superSet);
            }
        }
        return null;
    }

    public Map generateFrequentItemSet() {
        generateTIDSet();
        Set<ItemSet> lastFrequentItemSet = new HashSet<>();
        int index = 0;


        for(Map.Entry<ItemSet, Set<Integer>> entry: this.tidSets.entrySet()) {
            Set<Integer> tidSet= entry.getValue();
            if (tidSet.size() > this.minSupport) {
                int[] item = new int[1];
                item[0] = index;
                ItemSet frequentItemSet = new ItemSet(1, item);
                lastFrequentItemSet.add(frequentItemSet);
                this.frequentItemSet.put(frequentItemSet, tidSet.size());
            }
            index++;
        }

        while (lastFrequentItemSet.size() != 0) {
            Set<ItemSet> newFrequentItemSet = new HashSet<>();
            for (ItemSet cni : lastFrequentItemSet) {
                for (ItemSet cnj : lastFrequentItemSet) {
                    ItemSet superSet = generateSuperSet(cni, cnj);
                    if (superSet != null) {
                        Set<Integer> tidSuperSets = new HashSet<>();
                        tidSuperSets.addAll(tidSets.get(cni));
                        tidSuperSets.retainAll(tidSets.get(cnj));
                        if (tidSuperSets.size() >= this.minSupport) {
                            newFrequentItemSet.add(superSet);
                            tidSets.put(superSet, tidSuperSets);
                            frequentItemSet.put(superSet, tidSuperSets.size());
                        }
                    }
                }
            }
            lastFrequentItemSet.clear();
            lastFrequentItemSet = newFrequentItemSet;
        }
        return this.frequentItemSet;
    }
}
