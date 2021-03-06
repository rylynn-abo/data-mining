package edu.rylynn.datamining.core.associations;

import edu.rylynn.datamining.core.associations.common.ItemSet;

import java.util.*;

public class Apriori {
    private double minSupport;
    private double minConfidence;
    private Map<String, Integer> itemCount;
    private List<String> itemIndex;
    private List<String[]> itemData;
    private Map<ItemSet, Double> frequentItemSet;

    public Apriori(double minSupport, double minConfidence, List<String> data) {
        this.minSupport = minSupport;
        this.minConfidence = minConfidence;
        this.frequentItemSet = new HashMap<>();
        itemData = new ArrayList<>();
        itemCount = new HashMap<>();
        itemIndex = new ArrayList<>();

        for (String line : data) {
            itemData.add(line.split(","));
        }

    }

    public static void main(String[] args) {
        List<String> list = new ArrayList<String>();
        list.add("健康麦香包,皮蛋瘦肉粥,养颜红枣糕");
        list.add("健康麦香包,香煎葱油饼,皮蛋瘦肉粥,八宝粥");
        list.add("香煎葱油饼,皮蛋瘦肉粥,八宝粥");
        list.add("香煎葱油饼,八宝粥");
        new Apriori(0.5, 0.7, list).generateRules();
    }

    public List<String[]> getitemData() {
        return itemData;
    }

    public double getMinSupport() {
        return minSupport;
    }

    public void setMinSupport(double minSupport) {
        this.minSupport = minSupport;
    }

    public double getMinConfidence() {
        return minConfidence;
    }

    public void setMinConfidence(double minConfidence) {
        this.minConfidence = minConfidence;
    }

    private void firstScan() {
        int index = 1;
        for (String[] line : itemData) {
            for (int i = 0; i < line.length; i++) {
                if (itemCount.containsKey(line[i])) {
                    itemCount.put(line[i], itemCount.get(line[i]) + 1);
                } else {
                    itemCount.put(line[i], 1);
                    itemIndex.add(line[i]);
                }
            }

        }
        for (Map.Entry<String, Integer> entry : itemCount.entrySet()) {
            double thisConfidence = (double)entry.getValue() / itemData.size();
            if(thisConfidence >= minConfidence){
                int[] a = new int[1];
                a[0] = itemIndex.indexOf(entry.getKey());
                frequentItemSet.put(new ItemSet(1, a), thisConfidence);
            }
        }
    }


    private ItemSet generateSuperSet(ItemSet cn1, ItemSet cn2) {
        int[] cn1Item = cn1.getItem();
        int[] cn2Item = cn2.getItem();
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


    private double countItemSet(ItemSet itemSet) {
        int[] items = itemSet.getItem();
        int count = 0;
        for (String[] line : itemData) {
            int flag = 1;
            for (int i = 0; i < items.length; i++) {
                flag = 0;
                for (int j = 0; j < line.length; j++) {
                    if (itemIndex.indexOf(line[j]) == items[i]) {
                        flag = 1;
                        break;
                    }
                }
                if (flag == 0) {
                    break;
                }
            }
            if (flag == 1) {
                count++;
            }
        }
        return (double) count / (double) itemData.size();
    }

    private List<ItemSet> generateSubSet(ItemSet itemSet) {
        List<ItemSet> subSets = new ArrayList<>();
        int[] items = itemSet.getItem();
        int superSetLen = items.length;
        for (int i = 0; i < items.length; i++) {
            int k = 0;
            int[] subSetItems = new int[superSetLen - 1];
            for (int j = 0; j < items.length; j++) {
                if (i != j) {
                    subSetItems[k++] = items[j];
                }
            }
            subSets.add(new ItemSet(superSetLen - 1, subSetItems));
        }
        return subSets;
    }

    public Map<ItemSet, Double> getFrequentItemSet() {
        firstScan();
        Set<ItemSet> lastFrequentSet = new HashSet<>(frequentItemSet.keySet());
        while (lastFrequentSet.size() != 0) {//from C_(n-1) get C_n and the lastFrequentSet is the C_(n-1)
            Set<ItemSet> newFrequentSet = new HashSet<>();
            for (ItemSet cni : lastFrequentSet) {
                for (ItemSet cnj : lastFrequentSet) {
                    //get the candicate set
                    ItemSet superSet = generateSuperSet(cni, cnj);

                    if (superSet != null) {
                        double support = countItemSet(superSet);
                        if (support >= minSupport) {
                            //get the subset of the candicate set
                            //and judge if all the subset of the candicateset is in the frequentItemSet
                            List<ItemSet> subSets = generateSubSet(superSet);
                            int flag = 1;
                            for (ItemSet subSet : subSets) {
                                if (!this.frequentItemSet.containsKey(subSet)) {
                                    flag = 0;
                                    break;
                                }
                            }
                            if (flag == 1) {
                                if (!this.frequentItemSet.containsKey(superSet)) {
                                    frequentItemSet.put(superSet, support);
                                }
                                newFrequentSet.add(superSet);
                            }
                        }
                    }
                }
            }
            lastFrequentSet.clear();
            lastFrequentSet = newFrequentSet;
        }
        return frequentItemSet;
    }

    public void generateRules() {
        getFrequentItemSet();
        for (Map.Entry<ItemSet, Double> entry : frequentItemSet.entrySet()) {
            if (entry.getKey().getSize() > 1) {
                ItemSet frequentItems = entry.getKey();
                int[] items = frequentItems.getItem();
                List<Integer> items1 = new ArrayList<>();
                List<Integer> items2 = new ArrayList<>();
                double supportAB = frequentItemSet.get(frequentItems);
                System.out.print("{");
                for (int q : frequentItems.getItem()) {
                    System.out.print(itemIndex.get(q) + ",");
                }

                System.out.print("} ");
                System.out.println(", support: " + supportAB);

                for (int i = 1; i < (1 << items.length); i++) {
                    items1.clear();
                    items2.clear();
                    int index = 0;
                    int j = i;
                    while (index < items.length) {
                        if (j % 2 == 1) {
                            items1.add(items[index]);
                        } else {
                            items2.add(items[index]);
                        }
                        index++;
                        j /= 2;
                    }
                    if (items1.size() == 0 || items2.size() == 0) {
                        continue;
                    }
                    int[] item1Array = new int[items1.size()];
                    for (int ii = 0; ii < items1.size(); ii++) {
                        item1Array[ii] = items1.get(ii);
                    }
                    int[] item2Array = new int[items2.size()];
                    for (int ii = 0; ii < items2.size(); ii++) {
                        item2Array[ii] = items2.get(ii);
                    }
                    ItemSet A = new ItemSet(item1Array.length, item1Array);
                    ItemSet B = new ItemSet(item2Array.length, item2Array);

                    double supportA = frequentItemSet.get(A);
                    double supportB = frequentItemSet.get(B);
                    if (supportAB / supportA >= minConfidence) {
                        System.out.print("{");
                        for (int q : A.getItem()) {
                            System.out.print(itemIndex.get(q) + ",");
                        }
                        System.out.print("} ");
                        System.out.print(", support: " + supportA);

                        System.out.print(" ==> ");

                        System.out.print("{");
                        for (int q : B.getItem()) {
                            System.out.print(itemIndex.get(q) + ",");
                        }
                        System.out.print("} ");
                        System.out.print(", support " + supportB);

                        System.out.println("...   Condidence: " + supportAB / supportA);
                    }
                }
            }
            System.out.println();
        }
    }

    class ItemPair {
        private int i;
        private int j;
        private int hash;
        private int count;

        public ItemPair(int i, int j) {
            this.i = i;
            this.j = j;
            this.count = 0;
            this.hash = 0;
        }

        @Override
        public int hashCode() {
            int h = hash;
            if (hash == 0) {
                h = (i + j) * 4321 + (i * j) * 2980;
                hash = h;
            }
            return h;
        }
    }

}
