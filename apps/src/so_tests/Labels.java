package so_tests;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

public class Labels {
	static JFrame frame=new JFrame("Labels");
	static int janela_x=500, janela_y=500;
	// Botões e colocar no painel
	static JButton init = new JButton("Init");
	static JButton left = new JButton("Left");
	static JButton down = new JButton("Down");
	static JButton right = new JButton("Right");
	static JButton menu = new JButton("Menu");
	static Board grelha = Board.painel();
	public static void main(String[] args){
		frame.setTitle("Jogo 2048 em Java"); // Titulo da janela
		frame.setSize(janela_x, janela_y); // Define o tamanho da janela
		frame.setLocationRelativeTo(null); // Centraliza a janela no ecrã
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setBackground(Color.WHITE);
		//frame.setResizable(false); // Não deixa a janela ser aumentada
		// Painel Fundo
		JPanel fundo = new JPanel();
		fundo.setBackground(Color.WHITE);
		// Painel Botões
		JPanel botoes = new JPanel();
		GridLayout grid_botoes = new GridLayout(1, 5, 5, 5);
		botoes.setLayout(grid_botoes);
		botoes.setOpaque(true);
		botoes.setSize(360, 50);
		botoes.setLocation(70, 390);
		botoes.setBackground(Color.WHITE);
		botoes.add(init);
		botoes.add(left);
		botoes.add(down);
		botoes.add(right);
		botoes.add(menu);
		// Adicionar Panels à janela
		frame.getContentPane().add(botoes, BorderLayout.SOUTH);
		frame.getContentPane().add(grelha, BorderLayout.CENTER);
		frame.getContentPane().add(fundo, BorderLayout.NORTH);
		frame.setVisible(true);
		// ActionListener dos botões
		ActionListener accao_botoes = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int n = -1;
				if (e.getSource().equals(init)) n = 0;
				else if (e.getSource().equals(left)) n = 1;
				else if (e.getSource().equals(down)) n = 2;
				else if (e.getSource().equals(right)) n = 3;
				else n = 4;
				switch (n){
					case 0:
						grelha.novo_jogo();
						grelha.print();
						break;
					case 1:
						grelha.esquerda();
						grelha.print();
						break;
					case 2:
						grelha.baixo();
						grelha.print();
						break;
					case 3:
						grelha.direita();
						grelha.print();
						break;
					case 4:
						janela_menu();
						break;
					}
			}
		};
		init.addActionListener(accao_botoes);
		left.addActionListener(accao_botoes);
		down.addActionListener(accao_botoes);
		right.addActionListener(accao_botoes);
		menu.addActionListener(accao_botoes);
	}
	static void janela_menu() {
	}
	static class Board extends JPanel {
		int linhas=10;
		int colunas=10;
		int currentX=0;
		int currentY=0;
		JLabel[][] labels=new JLabel[linhas][colunas];
		public Board() {
			GridLayout grid_grelha = new GridLayout(linhas, colunas, 3, 3);
			setLayout(grid_grelha);
			setOpaque(true);
			setSize(janela_x - 140, janela_y-140);
			setLocation(70, 20);
			setBackground(Color.GREEN);
			// criar JLabels
			for (int num = 0; num < linhas; num++){
				for (int num2 = 0; num2 < colunas; num2++){
					JLabel label = new JLabel();
					//label.setText(String.format("%d,%d",num,num2));
					label.setOpaque(true);
					label.setBackground(select_cor(num,num2));
					label.setHorizontalAlignment(SwingConstants.CENTER);
					add(label);
					labels[num][num2]=label;
				}
			}
		}
		public void direita() {
			if (currentX+1 < colunas) ++currentX;
			JLabel label = labels[currentY][currentX];
			label.setBackground(Color.YELLOW);
		}
		public void baixo() {
			if (currentY+1 < linhas) ++currentY;
			JLabel label = labels[currentY][currentX];
			label.setBackground(Color.YELLOW);
		}
		public void novo_jogo() {
			currentX=currentY=0;
			for (int num = 0; num < linhas; num++){
				for (int num2 = 0; num2 < colunas; num2++){
					JLabel label = labels[num][num2];
					label.setBackground(select_cor(num,num2));
				}
			}
		}
		public void esquerda() {
			if (currentX > 0) --currentX;
			JLabel label = labels[currentY][currentX];
			label.setBackground(Color.YELLOW);
		}
		public void print() {
		}
		static Color select_cor(int a, int b) {
			return new Color((97*a)%256,(97*b)%256,(51*a*b)%256);
		}
		public static Board painel(){
			// Painel Grelha
			return new Board();
		}
	}
}
