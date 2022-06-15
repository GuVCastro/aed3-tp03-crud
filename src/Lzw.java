import java.io.DataOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
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

    public byte[] toByteArrayLzw( int id_conta, List<Integer> nome, List<Integer> cpf, List<Integer> cidade, int qtd_transferencias, float saldo) throws IOException {

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

        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);
        conta.id_conta = dis.readInt();
        i += 4;

        while (j < 3) {
            do {
                c = ba[i];
                texto.add((int) ba[i] & 0xff);
                i++;
            } while (c != ';');

            int index = texto.size() - 1;
            texto.remove(index);

            if (j == 0) conta.nome = descompressao_lzw(texto);
            else if (j == 1) conta.cpf = descompressao_lzw(texto);
            else if (j == 2) conta.cidade = descompressao_lzw(texto);

            texto.clear();
            j++;
        }
            
        int tam_ba = ba.length;
        byte ba_restante[] = new byte[tam_ba - i];
        int k;
        for (j = i, k = 0; j < tam_ba; j++, k++) {
            ba_restante[k] = ba[i];
        }

        bais = new ByteArrayInputStream(ba_restante);
        dis = new DataInputStream(bais);
        conta.qtd_transferencias = dis.readInt();
        conta.saldo = dis.readFloat();

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
}
