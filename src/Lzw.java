import java.io.DataOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class Lzw {
    private byte[] converte_bytes(List<Integer> texto_comprimido) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        for (int i = 0; i < texto_comprimido.size(); i++) {
            dos.writeByte(texto_comprimido.get(i));
        }
        dos.writeByte(';');

        return baos.toByteArray();
    }

    // private String decodifica(List<Integer> lista) {
    //     List lista_decodificada = new ArrayList<>();

    //     for (int i = 0; i < lista.size(); i ++) {
    //         lista_decodificada.add()
    //     }

    //     return 
    // }

    public byte[] toByteArrayLzw(   int id_conta,
                                    List<Integer> nome,
                                    List<Integer> cpf,
                                    List<Integer> cidade,
                                    int qtd_transferencias,
                                    float saldo) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(id_conta);
        dos.write(converte_bytes(nome));
        dos.write(converte_bytes(cpf));
        dos.write(converte_bytes(cidade));
        dos.writeInt(qtd_transferencias);
        dos.writeFloat(saldo);

        return baos.toByteArray();
    }

    public Contas fromByteArrayLzw(byte ba[]) throws IOException {
        Contas conta = new Contas();

        List<Integer> texto = new ArrayList<>();
        int i = 0, j = 0;
        byte c = ' ';

        while (j < 3) {
            do {
                c = ba[i];
                texto.add((int) ba[i] & 0xff);
                i++;
            } while (c != ';');

            if (j == 0) conta.nome = descompressao_lzw(texto);
            else if (j == 1) conta.cpf = descompressao_lzw(texto);
            else if (j == 2) conta.cidade = descompressao_lzw(texto);

            texto.clear();
            j++;
        }

        return conta;
    }

    /** Compress a string to a list of output symbols. */
    public List<Integer> compressao_lzw(String uncompressed) {
        // Build the dictionary.
        int dictSize = 128;
        Map<String,Integer> dictionary = new HashMap<String, Integer>();
        for (int i = 0; i < 128; i++)
            dictionary.put("" + (char)i, i);
    
        String w = "";
        List<Integer> result = new ArrayList<Integer>();
        for (char c : uncompressed.toCharArray()) {
            String wc = w + c;
            if (dictionary.containsKey(wc))
                w = wc;
            else {
                result.add(dictionary.get(w));
                // Add wc to the dictionary.
                dictionary.put(wc, dictSize++);
                w = "" + c;
            }
        }
    
        // Output the code for w.
        if (!w.equals(""))
            result.add(dictionary.get(w));
        return result;
    }

    /** Decompress a list of output ks to a string. */
    public String descompressao_lzw(List<Integer> compressed) {
        // Build the dictionary.
        int dictSize = 128;
        Map<Integer,String> dictionary = new HashMap<Integer,String>();
        for (int i = 0; i < 128; i++)
            dictionary.put(i, "" + (char)i);
    
        String w = "" + (char)(int)compressed.remove(0);
        StringBuffer result = new StringBuffer(w);
        for (int k : compressed) {
            String entry;
            if (dictionary.containsKey(k))
                entry = dictionary.get(k);
            else if (k == dictSize)
                entry = w + w.charAt(0);
            else
                throw new IllegalArgumentException("Bad compressed k: " + k);
    
            result.append(entry);
    
            // Add w+entry[0] to the dictionary.
            dictionary.put(dictSize++, w + entry.charAt(0));
    
            w = entry;
        }
        return result.toString();
    }    

    // public ArrayList<Integer> compressao_lzw(String s1) {
    //     ArrayList<Integer> lista = new ArrayList<>(); // gera a sequência de dados comprimidos
    //     Map<String, Integer> dicionario = new HashMap<String, Integer>();
    //     for (int i = 0; i <= 255; i++) // cria o dicionário com as 256 símbolos
    //         dicionario.put("" + (char) i, i);
    //     String p = "", c = "";
    //     p += s1.charAt(0);
    //     int code = 256;
    //     for (int i = 0; i < s1.length(); i++) { // comprime a palavra original e vai modificando o dicionário conforme a
    //                                             // necessidade
    //         if (i != s1.length() - 1)
    //             c += s1.charAt(i + 1);
    //         if (dicionario.get(p + c) != dicionario.get(dicionario.size())) {
    //             p = p + c;
    //         } else {
    //             lista.add(dicionario.get(p));
    //             dicionario.put(p + c, code);
    //             code++;
    //             p = c;
    //         }
    //         c = "";
    //     }
    //     lista.add(dicionario.get(p)); // adiciona o último valor na lista
    //     System.out.print(lista.toString());
    //     decodifica_lzw(lista);
    //     return lista;
    // }

    // private String decodifica_lzw(ArrayList<Integer> op) {
    //     String descomprimida = "";
    //     Map<Integer, String> dicionario = new HashMap<Integer, String>();
    //     for (int i = 0; i <= 122; i++) // cria o dicionário
    //         dicionario.put(i, "" + (char) i);
    //     int old = op.get(0), n;
    //     String s = dicionario.get(old);
    //     String c = "";
    //     c += s.charAt(0); // primeiro valor da palavra descomprimida
    //     System.out.print(s);
    //     descomprimida += s;
    //     int count = 256;
    //     for (int i = 0; i < op.size() - 1; i++) {
    //         n = op.get(i + 1);
    //         if (dicionario.get(n) == dicionario.get(dicionario.size())) {
    //             s = dicionario.get(old);
    //             s = s + c;
    //         } else {
    //             s = dicionario.get(n);
    //         }
    //         c = "";
    //         descomprimida += s;
    //         System.out.print(s); // s é a letra atual da palavra que está sendo descomprimida
    //         c += s.charAt(0);
    //         dicionario.put(count, dicionario.get(old) + c);
    //         count++;
    //         old = n;
    //     }
    //     System.out.println();
    //     return descomprimida;
    // }

    /*
     * public List<Integer> compressao_lzw(String text) {
     * int dictSize = 256;
     * Map<String, Integer> dictionary = new HashMap<>();
     * 
     * for (int i = 0; i < dictSize; i++) {
     * dictionary.put(String.valueOf((char) i), i);
     * }
     * 
     * String foundChars = "";
     * List<Integer> result = new ArrayList<>();
     * 
     * for (char character : text.toCharArray()) {
     * String charsToAdd = foundChars + character;
     * 
     * if (dictionary.containsKey(charsToAdd)) {
     * foundChars = charsToAdd;
     * } else {
     * result.add(dictionary.get(foundChars));
     * dictionary.put(charsToAdd, dictSize++);
     * foundChars = String.valueOf(character);
     * }
     * 
     * if (!foundChars.isEmpty()) {
     * result.add(dictionary.get(foundChars));
     * }
     * }
     * 
     * return result;
     * }
     * 
     * public String descompressao_lzw(ArrayList<Integer> encodedText) {
     * int dictSize = 256;
     * Map<Integer, String> dictionary = new HashMap<>();
     * 
     * for (int i = 0; i < dictSize; i++) {
     * dictionary.put(i, String.valueOf((char) i));
     * }
     * 
     * String characters = String.valueOf((char) encodedText.remove(0).intValue());
     * StringBuilder result = new StringBuilder(characters);
     * 
     * for (int code : encodedText) {
     * String entry = dictionary.containsKey(code)
     * ? dictionary.get(code)
     * : characters + characters.charAt(0);
     * result.append(entry);
     * dictionary.put(dictSize++, characters + entry.charAt(0));
     * characters = entry;
     * }
     * 
     * return result.toString();
     * }
     */
}
