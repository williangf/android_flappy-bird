package com.udemy.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

import sun.rmi.runtime.Log;

public class FlappyBird extends ApplicationAdapter {

	//Preferences
	Preferences pref;
	//-----------

	private SpriteBatch batch;
	private Texture[] bird;
	private Texture fundo;
	private Texture canoTop;
	private Texture canoBot;
	private Texture gameOver;
	private Texture clouds;
	private BitmapFont fonte;
	private BitmapFont mensagem;
	private BitmapFont msgTopScore;
	private BitmapFont topScore;
	private BitmapFont nivel;
	private Circle birdCircle;
	private Rectangle rectangleCanoTop;
	private Rectangle rectangleCanoBot;
	private ShapeRenderer shape;
	private Sound flap;
	private Sound beat;
	private Music music;
	private boolean bateu;

	private Random numeroRandom;

	//Atributos de Configuração
	private float larguraTela;
	private float alturaTela;
	private int estadoJogo = 0; // 0 = Jogo não iniciado // 1 = Jogo iniciado // 2 = Game Over
	private int pontuacao = 0;


	private float animacaoBackground = 0;
	private float variacao = 0;
	private float gravidade = 0;
	private float posicaoInicialVertical = 0;
	private float posicaoMovimentoCanoHorizontal;
	private float espacamento;
	private float deltaTime;
	private float alturaCanosRandom;
	private boolean marcouPonto;

	//Camera
	private OrthographicCamera camera;
	private Viewport viewport;
	private final float VIRTUAL_WIDTH = 768;
	private final float VIRTUAL_HEIGHT = 1024;

	//**************************************************************
	//					Configurações Gerais					  \\
	//**************************************************************
	//--Gravidade
	private float difGravidade   = (float) 1;   // Default = 1

	//--Espaçamento
	private float difEspacamento = 50; 		// Default = 300

	//--Animação Background
	private float difBackground  = 1; 			// Default = 1;

	//--Níveis
	int[] inicante 		= {0,5};
	int[] novato 		= {5,10};
	int[] aprendiz		= {10,25};
	int[] amador 		= {25,35};
	int[] intermediario = {35,40};
	int[] avancado 		= {40,55};
	int[] experiente 	= {55,90};
	int[] profissional 	= {90,150};
	int[] mestre 		= {150,500};
	int[] yoda 			= {500,1000};
	//**************************************************************
	
	@Override
	public void create () {

		pref = Gdx.app.getPreferences("br.com.udemy.settings");

		if(pegarPontuacao() == ""){
			pref.putString("pontuacao", "0");
			pref.flush();
		}

		batch = new SpriteBatch();

		flap = Gdx.audio.newSound(Gdx.files.internal("sounds/flap.wav"));
		beat = Gdx.audio.newSound(Gdx.files.internal("sounds/beat.wav"));

		music = Gdx.audio.newMusic(Gdx.files.internal("sounds/music.wav"));

		music.setLooping(true);
		music.play();

		music.setVolume(1);

		numeroRandom = new Random();
		birdCircle = new Circle();
		rectangleCanoTop = new Rectangle();
		rectangleCanoBot = new Rectangle();
		shape = new ShapeRenderer();

		fonte = new BitmapFont();
		fonte.setColor(Color.WHITE);
		fonte.getData().setScale(5);

		mensagem = new BitmapFont();
		mensagem.setColor(Color.WHITE);
		mensagem.getData().setScale(3);

		nivel = new BitmapFont();
		nivel.getData().setScale(2);

		msgTopScore = new BitmapFont();
		msgTopScore.setColor(Color.ORANGE);
		msgTopScore.getData().setScale(4);

		topScore = new BitmapFont();
		topScore.setColor(Color.YELLOW);
		topScore.getData().setScale(5);

		bird = new Texture[3];
		bird[0] = new Texture("passaro1.png");
		bird[1] = new Texture("passaro2.png");
		bird[2] = new Texture("passaro3.png");

		fundo = new Texture("fundo.png");

		canoTop = new Texture("cano_topo.png");
		canoBot = new Texture("cano_baixo.png");
		gameOver = new Texture("game_over.png");
		clouds = new Texture("clouds.png");

		//Configuração da Câmera

		camera = new OrthographicCamera();
		camera.position.set(VIRTUAL_WIDTH/2, VIRTUAL_HEIGHT/2, 0);
		viewport = new FillViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);

		//----------------------

		larguraTela = VIRTUAL_WIDTH;
		alturaTela  = VIRTUAL_HEIGHT;

		posicaoInicialVertical = alturaTela / 2;
		posicaoMovimentoCanoHorizontal = larguraTela;

		espacamento = difEspacamento;  //300 DEFAULT

	}

	@Override
	public void render () {

		animacaoBackground += difBackground;

		camera.update();

		//Limpar frames anteriores
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_COLOR_BUFFER_BIT);

		deltaTime = Gdx.graphics.getDeltaTime();

		variacao += deltaTime * 10;

		if(variacao > 2){
			variacao = 0;
		}

		if(estadoJogo == 0){

			if(Gdx.input.justTouched()){
				estadoJogo = 1;
				flap.play();
			}

		}else{
			gravidade += difGravidade;
			if(posicaoInicialVertical > 0 || gravidade < 0){
				posicaoInicialVertical = posicaoInicialVertical - gravidade;
			}

			if(estadoJogo == 1){

				posicaoMovimentoCanoHorizontal -= deltaTime * 400;

				if(Gdx.input.justTouched()){
					gravidade = (float) - (difGravidade*20);
					flap.play();

					Gdx.app.log("TESTE",String.valueOf(gravidade));
				}

				//Verifica se o cano saiu inteiramente da tela
				if(posicaoMovimentoCanoHorizontal < -canoTop.getWidth()){
					posicaoMovimentoCanoHorizontal = larguraTela;
					alturaCanosRandom = numeroRandom.nextInt(400) -200;
					marcouPonto = false;
				}

				//Verifica Pontuação
				if(posicaoMovimentoCanoHorizontal < 100){
					if(!marcouPonto){
						pontuacao++;
						marcouPonto = true;
					}
				}

			}else{
				String pont = pegarPontuacao();

				if(Integer.parseInt(pont) < pontuacao){
					pref.putString("pontuacao", toString().valueOf(pontuacao));
					pref.flush();
				}

				if(Gdx.input.justTouched()){
					estadoJogo = 0;
					marcouPonto = false;
					pontuacao = 0;
					gravidade = 0;
					posicaoInicialVertical = alturaTela / 2;
					posicaoMovimentoCanoHorizontal = larguraTela;
					bateu = false;

					pont = pref.getString("pontuacao","");

				}
			}
		}

		//Configurar dados de projeção da câmera
		batch.setProjectionMatrix(camera.combined);

		batch.begin();

		fundo.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
		batch.draw(fundo, 0, 0, (int) animacaoBackground ,0, (int) larguraTela, (int) alturaTela);

		//batch.draw(clouds, 0, 0,(int) animacaoBackground ,0, (int) larguraTela, (int) alturaTela);

		batch.draw(canoTop, posicaoMovimentoCanoHorizontal, alturaTela / 2 + espacamento / 2 + alturaCanosRandom);
		batch.draw(canoBot, posicaoMovimentoCanoHorizontal, alturaTela / 2 - canoBot.getHeight() - espacamento / 2 + alturaCanosRandom);

		batch.draw(bird[ (int) variacao ], 120, posicaoInicialVertical);

		fonte.draw(batch, String.valueOf(pontuacao), larguraTela / 2 - 20, alturaTela - 50);

		String pont = pegarPontuacao();
		if(estadoJogo == 2){
			if(!(Integer.parseInt(pont) > pontuacao)){
				msgTopScore.draw(batch,"New Record!",larguraTela / 2 - 165, 750);
			}
			batch.draw(gameOver, larguraTela / 2 - gameOver.getWidth() / 2, alturaTela / 2);
			mensagem.draw(batch,"Toque para Reiniciar", larguraTela / 2 - 200, alturaTela / 2 - gameOver.getHeight() / 2);
		}

		if(estadoJogo == 0){
			pont = pegarPontuacao();
			mensagem.draw(batch,"Toque para Iniciar", larguraTela / 2 - 175, alturaTela / 2);
			topScore.draw(batch,pont,larguraTela / 2 - 20, 150);
			msgTopScore.draw(batch,"Top Score",larguraTela / 2 - 135, 250);

			GlyphLayout layout = new GlyphLayout(msgTopScore, "Top Score");
			float textWidth = layout.width;

			if(Integer.parseInt(pont) < inicante[0]){
				nivel.setColor(Color.CYAN);
				nivel.draw(batch,"Iniciante",130, alturaTela - 30);

			}else if(Integer.parseInt(pont) >= novato[0] && Integer.parseInt(pont) < novato[1]){
				nivel.setColor(Color.WHITE);
				nivel.draw(batch,"Novato",130, alturaTela - 30);

			}else if(Integer.parseInt(pont) >= aprendiz[0] && Integer.parseInt(pont) < aprendiz[1]){
				nivel.setColor(Color.BLUE);
				nivel.draw(batch,"Aprendiz",130, alturaTela - 30);

			}else if(Integer.parseInt(pont) >= amador[0] && Integer.parseInt(pont) < amador[1]){
				nivel.setColor(Color.CHARTREUSE);
				nivel.draw(batch,"Amador",130, alturaTela - 30);

			}else if(Integer.parseInt(pont) >= intermediario[0] && Integer.parseInt(pont) < intermediario[1]){
				nivel.setColor(Color.YELLOW);
				nivel.draw(batch,"Intermediário",130, alturaTela - 30);

			}else if(Integer.parseInt(pont) >= avancado[0] && Integer.parseInt(pont) < avancado[1]){
				nivel.setColor(Color.RED);
				nivel.draw(batch,"Avançado",130, alturaTela - 30);

			}else if(Integer.parseInt(pont) >= experiente[0] && Integer.parseInt(pont) < experiente[1]){
				nivel.setColor(Color.BROWN);
				nivel.draw(batch,"Experiente",130, alturaTela - 30);

			}else if(Integer.parseInt(pont) >= profissional[0] && Integer.parseInt(pont) < profissional[1]){
				nivel.setColor(Color.BLACK);
				nivel.draw(batch,"Profissional",130, alturaTela - 30);

			}else if(Integer.parseInt(pont) >= mestre[0] && Integer.parseInt(pont) < mestre[1]){
				nivel.setColor(Color.GOLD);
				nivel.draw(batch,"Mestre",130, alturaTela - 30);

			}else if(Integer.parseInt(pont) >= yoda[0] && Integer.parseInt(pont) < yoda[1]){
				nivel.setColor(Color.GREEN);
				nivel.draw(batch,"YODA",130, alturaTela - 30);
			}
		}

		batch.end();

		birdCircle.set(120 + bird[0].getWidth() / 2, posicaoInicialVertical + bird[0].getHeight() / 2, bird[0].getWidth() / 2);
		rectangleCanoBot = new Rectangle(
			posicaoMovimentoCanoHorizontal,
				alturaTela / 2 - canoBot.getHeight() - espacamento / 2 + alturaCanosRandom,
				canoBot.getWidth(), canoBot.getHeight()
		);

		rectangleCanoTop = new Rectangle(
			posicaoMovimentoCanoHorizontal,
				alturaTela / 2 + espacamento / 2 + alturaCanosRandom,
				canoTop.getWidth(), canoTop.getHeight()
		);

//		//Desenhar Formas
//		shape.begin(ShapeRenderer.ShapeType.Filled);
//		shape.circle( birdCircle.x, birdCircle.y, birdCircle.radius );
//		shape.rect(rectangleCanoBot.x, rectangleCanoBot.y, rectangleCanoBot.width, rectangleCanoBot.height);
//		shape.setColor(Color.BLACK);
//		shape.end();


		//Colisão
		if(Intersector.overlaps( birdCircle, rectangleCanoBot) || Intersector.overlaps( birdCircle, rectangleCanoTop) || posicaoInicialVertical <= 0 || posicaoInicialVertical >= alturaTela){
			estadoJogo = 2;
			if(!bateu){
				beat.play();
				bateu = false;
			}
			bateu = true;
		}

	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
	}

	@Override
	public void dispose () {

	}

	public String pegarPontuacao(){
		String pont = pref.getString("pontuacao","");
		return pont;
	}
}
