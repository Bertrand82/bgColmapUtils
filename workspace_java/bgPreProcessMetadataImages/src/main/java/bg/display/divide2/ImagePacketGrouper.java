package bg.display.divide2;

import java.util.*;

public class ImagePacketGrouper {

    private final int[][] coVisibility;

    public ImagePacketGrouper(int[][] coVisibility) {
        this.coVisibility = coVisibility;
    }

    /**
     * Connectivité d'une image i :
     * somme des co-visibilités avec les autres images non assignées.
     */
    public int connectivity(int image, Set<Integer> remaining) {
        int sum = 0;
        for (int j : remaining) {
            if (j != image) {
                sum += coVisibility[image][j];
            }
        }
        return sum;
    }

    /**
     * Choisit l'image graine avec la plus forte connectivité.
     */
    public int chooseSeed(Set<Integer> remaining) {
        int bestImage = -1;
        int bestScore = -1;

        for (int image : remaining) {
            int score = connectivity(image, remaining);
            if (score > bestScore) {
                bestScore = score;
                bestImage = image;
            }
        }

        return bestImage;
    }

    /**
     * Score d'une image candidate par rapport à un groupe courant :
     * somme des co-visibilités avec toutes les images du groupe.
     */
    public int scoreWithGroup(int candidate, List<Integer> group) {
        int sum = 0;
        for (int image : group) {
            sum += coVisibility[candidate][image];
        }
        return sum;
    }

    /**
     * Construit des groupes disjoints de taille packetSize avec stratégie gloutonne.
     */
    public List<List<Integer>> buildPackets(int packetSize) {
        List<List<Integer>> packets = new ArrayList<>();
        Set<Integer> remaining = new LinkedHashSet<>();

        int n = coVisibility.length;
        for (int i = 0; i < n; i++) {
            remaining.add(i);
        }

        while (!remaining.isEmpty()) {
            List<Integer> group = new ArrayList<>();

            // 1) choisir la graine
            int seed = chooseSeed(remaining);
            group.add(seed);
            remaining.remove(seed);

            // 2) compléter le groupe
            while (group.size() < packetSize && !remaining.isEmpty()) {
                int bestCandidate = -1;
                int bestScore = -1;

                for (int candidate : remaining) {
                    int score = scoreWithGroup(candidate, group);
                    if (score > bestScore) {
                        bestScore = score;
                        bestCandidate = candidate;
                    }
                }

                group.add(bestCandidate);
                remaining.remove(bestCandidate);
            }

            packets.add(group);
        }

        return packets;
    }

    public static void main(String[] args) {
    	int paquetSize=2;
        int[][] covisibilité = {
            {0, 20, 15, 1, 0},
            {20, 0, 18, 2, 1},
            {15, 18, 0, 1, 0},
            {1, 2, 1, 0, 25},
            {0, 1, 0, 25, 0}
        };

        ImagePacketGrouper grouper = new ImagePacketGrouper(covisibilité);

        List<List<Integer>> packets = grouper.buildPackets(paquetSize);

        System.out.println("Paquets construits | paquetSize:"+paquetSize);
        for (int i = 0; i < packets.size(); i++) {
            System.out.println("Paquet " + i + " : " + packets.get(i));
        }
    }
}