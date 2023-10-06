package br.senai.sp.jandira.upload_firebase

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import br.senai.sp.jandira.upload_firebase.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class MainActivity : AppCompatActivity() {
    //Declaração dos atributos:

    //activity main binding - manipulação dos elementos gráficos do material design
    private lateinit var binding : ActivityMainBinding

    //storage reference - permite a manipulação do cloud storage (armazena arquivos)
    private lateinit var storageRef : StorageReference

    //firebase firestore - permite a manipulação no banco de dados noSQL
    private lateinit var firebaseFireStore : FirebaseFirestore

    //uri - permite a manipulação de arquivos através do seu endereçamento
    private var imageUri : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //layout montado/inflado
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root) //procurar a contraparte de MainAcitivity lá na raiz

        initVars()
        registerClickEvents()
    }

    // função de inicialização dos recursos do firebase
    private fun initVars(){
        storageRef = FirebaseStorage.getInstance().reference.child("images")
        firebaseFireStore = FirebaseFirestore.getInstance()
    }

    //função para o lançador de recuperação de imagens da galeria
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.GetContent()
    ){
        imageUri = it
        binding.imageView.setImageURI(it)
    }

    //função de tratamento de cliques

    private fun registerClickEvents(){
        //trata o evento de clique do componente imageView
        binding.imageView.setOnClickListener{
            resultLauncher.launch("image/*")
        }
        //trata o evento de clique do botão de upload
        binding.uploadBtn.setOnClickListener {
            uploadImage()
        }
        //trata o evento de clique do botão listar
        binding.showAllBtn.setOnClickListener {
            startActivity(Intent(this, ImagesFeed::class.java))
        }
    }

    //função de upload
    private fun uploadImage(){

        binding.progressBar.visibility = View.VISIBLE

        //define um nome unico para a imagem com o uso de um valor timestamp
        storageRef = storageRef.child(System.currentTimeMillis().toString())

        //executa o processo de upload da imagem V1
//        imageUri?.let {
//            storageRef.putFile(it).addOnCompleteListener{
//               task-> //o que veio da tarefa
//                    if (task.isSuccessful){
//                        Toast.makeText(this, "Upload concluído!", Toast.LENGTH_LONG).show()
//                    } else {
//                        Toast.makeText(this, "Erro ao realizar o upload", Toast.LENGTH_LONG).show()
//                    }
//
//                binding.progressBar.visibility = View.GONE
//                binding.imageView.setImageResource(R.drawable.upload) //volta para imagem inicial
//
//            }
//        }
//
        //EXECUTA O PROCESSO DE UPLOAD DA IMAGEM V2 - UPLOAD NO STORAGE E GRAVAÇÃO NO FIRESTORE
        imageUri?.let {
            storageRef.putFile(it).addOnCompleteListener { task->

                if (task.isSuccessful) {

                    //pega o endereço de download da imagem
                    storageRef.downloadUrl.addOnSuccessListener { uri ->

                        val map = HashMap<String, Any>()
                        map["pic"] = uri.toString()

                        firebaseFireStore.collection("images").add(map).addOnCompleteListener { firestoreTask ->

                            if (firestoreTask.isSuccessful){
                                Toast.makeText(this, "UPLOAD CONCLUÍDO", Toast.LENGTH_SHORT).show()

                            }else{
                                Toast.makeText(this, firestoreTask.exception?.message, Toast.LENGTH_SHORT).show()

                            }
                            binding.progressBar.visibility = View.GONE
                            binding.imageView.setImageResource(R.drawable.upload)

                        }
                    }

                }else{

                    Toast.makeText(this,  task.exception?.message, Toast.LENGTH_SHORT).show()

                }

                //BARRA DE PROGRESSO DO UPLOAD
                binding.progressBar.visibility = View.GONE

                //TROCA A IMAGEM PARA A IMAGEM PADRÃO
                binding.imageView.setImageResource(R.drawable.upload)

            }
        }
    }

}