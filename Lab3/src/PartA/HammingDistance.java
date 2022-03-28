package PartA;

public class HammingDistance {
    public static void main(String[] args) {
        System.out.println(hammingDistance(Integer.MAX_VALUE,1));
    }
    public static int hammingDistance(int x, int y) {
        int distance = 0;
        String binary_x = decimalToBinary(x);
        System.out.println(binary_x);
        String binary_y = decimalToBinary(y);
        System.out.println(binary_y);
        for (int i = 0;i < binary_x.length();i++){
            if (binary_x.charAt(i) != binary_y.charAt(i)) distance ++;
        }
        return distance;
    }
    public static String decimalToBinary(int num) {
        StringBuilder binStr = new StringBuilder();
        for(int i = 11-1;i >= 0; i--){
            binStr.append(num >>> i & 1);
        }
        return binStr.toString();
    }

}
