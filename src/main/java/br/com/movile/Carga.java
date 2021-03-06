package br.com.movile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import br.com.movile.cliente.model.Cliente;
import br.com.movile.estabelecimento.model.Estabelecimento;
import br.com.movile.motoboy.model.MotoBoy;
import br.com.movile.produto.model.Produto;

public class Carga {

	public static void main(String[] args) {
		Carga carga = new Carga();
		carga.cargaGeral();
	}

	Path caminho = null;
	List<String> allLines;
	List<Cliente> clientes;
	List<MotoBoy> motoboy;
	List<Estabelecimento> estabelecimentos;
	List<Produto> produtos;

	private static final char DEFAULT_SEPARATOR = ',';
	private static final char DEFAULT_QUOTE = '"';

	public void cliente() {
		caminho = Paths.get(System.getProperty("user.home") + "\\codenation\\mapfood\\clientes.csv");

		try {
			allLines = Files.readAllLines(caminho, StandardCharsets.UTF_8);
			clientes = new ArrayList<>();
			allLines.stream().skip(1).forEach(x -> {

				List<String> parseLine = parseLine(x, DEFAULT_SEPARATOR, DEFAULT_QUOTE);

				long id = Long.parseLong(parseLine.get(0).replaceAll(",", ""));
				double longitude = Double.parseDouble(parseLine.get(1));
				double latitude = Double.parseDouble(parseLine.get(2));

				clientes.add(new Cliente(id, longitude, latitude));
			});

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public void motoboy() {
		caminho = Paths.get(System.getProperty("user.home") + "\\codenation\\mapfood\\motoboys.csv");

		try {
			allLines = Files.readAllLines(caminho, StandardCharsets.UTF_8);
			motoboy = new ArrayList<>();
			allLines.stream().skip(1).forEach(x -> {

				List<String> parseLine = parseLine(x, DEFAULT_SEPARATOR, DEFAULT_QUOTE);

				long id = Long.parseLong(parseLine.get(0).replaceAll(",", ""));
				double longitude = Double.parseDouble(parseLine.get(1));
				double latitude = Double.parseDouble(parseLine.get(2));

				motoboy.add(new MotoBoy(id, longitude, latitude));
			});

		} catch (Exception e1) {
			System.out.println("Deu ruim no Motoboy : ");
			e1.printStackTrace();
		}

	}

	public void estabelecimentoPorMunicipio() {

		caminho = Paths
				.get(System.getProperty("user.home") + "\\codenation\\mapfood\\estabelecimento-por-municipio.csv");

		estabelecimentos = new ArrayList<>();

		try {

			allLines = Files.readAllLines(caminho, StandardCharsets.UTF_8);

			allLines.stream().skip(1).forEach(x -> {
				List<String> parseLine = parseLine(x, DEFAULT_SEPARATOR, DEFAULT_QUOTE);

				if (parseLine.size() == 1) {
					String li = "\"".concat(parseLine.get(0)).split(",")[0].concat("\"");
					String[] split = parseLine.get(0).split(",");
					split[0] = li;
					StringBuilder builder = new StringBuilder();
					for (String string : split) {
						builder.append(string.concat(","));
					}
					parseLine.clear();
					parseLine = parseLine(builder.toString(), DEFAULT_SEPARATOR, DEFAULT_QUOTE);
				}

				String id = parseLine.get(0);
				String nomeRestaurante = parseLine.get(1);
				String cidade = parseLine.get(2);
				double longitude = Double.parseDouble(parseLine.get(3));
				double latitude = Double.parseDouble(parseLine.get(4));
				String descricao = parseLine.get(5).replaceAll(";", "");

				estabelecimentos.add(new Estabelecimento(id, nomeRestaurante, cidade, longitude, latitude, descricao));
			});

			produtosPorEstabelecimento();
			produtos.stream().forEach(x -> {
				estabelecimentos.stream().forEach(j -> {
					if(x.getRestauranteId().equals(j.getId()))
						j.getProdutos().add(x);
				});
			});	
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}

	}

	public void produtosPorEstabelecimento() {
		caminho = Paths
				.get(System.getProperty("user.home") + "\\codenation\\mapfood\\produtos-por-estabelecimento.csv");

		try {
			produtos = new ArrayList<>();
			allLines = Files.readAllLines(caminho, StandardCharsets.UTF_8);

			allLines.stream().skip(1).forEach(x -> {

				List<String> parseLine = parseLine(x, DEFAULT_SEPARATOR, DEFAULT_QUOTE);

				String id = parseLine.get(1);
				String descricao = parseLine.get(0);
				String restauranteId = parseLine.get(2);
				String restaurante = parseLine.get(3);
				String classificacao = parseLine.get(4);
				BigDecimal precoUnitario = new BigDecimal(parseLine.get(5));
				String cidade = parseLine.get(6);

				produtos.add(
						new Produto(id, descricao, restauranteId, restaurante, classificacao, precoUnitario, cidade));
			});

		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Errrrooooooo");
		} catch (NumberFormatException e) {
			e.printStackTrace();
			System.out.println("Errooooooo");
		}
	}

	public static List<String> parseLine(String cvsLine, char separators, char customQuote) {

		List<String> result = new ArrayList<>();

		// if empty, return!
		if (cvsLine == null || cvsLine.isEmpty()) {
			return result;
		}

		if (customQuote == ' ') {
			customQuote = DEFAULT_QUOTE;
		}

		if (separators == ' ') {
			separators = DEFAULT_SEPARATOR;
		}

		StringBuffer curVal = new StringBuffer();
		boolean inQuotes = false;
		boolean startCollectChar = false;
		boolean doubleQuotesInColumn = false;

		char[] chars = cvsLine.toCharArray();

		for (char ch : chars) {

			if (inQuotes) {
				startCollectChar = true;
				if (ch == customQuote) {
					inQuotes = false;
					doubleQuotesInColumn = false;
				} else {

					// Fixed : allow "" in custom quote enclosed
					if (ch == '\"') {
						if (!doubleQuotesInColumn) {
							curVal.append(ch);
							doubleQuotesInColumn = true;
						}
					} else {
						curVal.append(ch);
					}

				}
			} else {
				if (ch == customQuote) {

					inQuotes = true;

					// Fixed : allow "" in empty quote enclosed
					if (chars[0] != '"' && customQuote == '\"') {
						curVal.append('"');
					}

					// double quotes in column will hit this!
					if (startCollectChar) {
						curVal.append('"');
					}

				} else if (ch == separators) {

					result.add(curVal.toString());

					curVal = new StringBuffer();
					startCollectChar = false;

				} else if (ch == '\r') {
					// ignore LF characters
					continue;
				} else if (ch == '\n') {
					// the end, break!
					break;
				} else {
					curVal.append(ch);
				}
			}

		}

		result.add(curVal.toString());

		return result;
	}

	public void cargaGeral() {
		cliente();
		motoboy();
		estabelecimentoPorMunicipio();
	}
	
	public Path getCaminho() {
		return caminho;
	}

	public List<String> getAllLines() {
		return allLines;
	}

	public List<Cliente> getClientes() {
		return clientes;
	}

	public List<MotoBoy> getMotoboy() {
		return motoboy;
	}

	public List<Estabelecimento> getEstabelecimentos() {
		return estabelecimentos;
	}

	public List<Produto> getProdutos() {
		return produtos;
	}

	public static char getDefaultSeparator() {
		return DEFAULT_SEPARATOR;
	}

	public static char getDefaultQuote() {
		return DEFAULT_QUOTE;
	}

}
