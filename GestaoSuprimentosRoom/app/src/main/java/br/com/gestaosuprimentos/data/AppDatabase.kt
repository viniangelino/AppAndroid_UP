package br.com.gestaosuprimentos.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import java.time.LocalDate

@Database(entities = [Cliente::class, Fornecedor::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun clienteDao(): ClienteDao
    abstract fun fornecedorDao(): FornecedorDao

    companion object {
        // @Volatile: todas as threads enxergam o valor mais novo da instância
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /** Singleton: o app inteiro usa sempre o mesmo banco. */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gestao_suprimentos.db"
                )
                    .addCallback(DadosIniciais)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

/**
 * Roda UMA vez, só quando o arquivo do banco é criado (primeira vez que o app abre).
 * Coloca alguns exemplos para a apresentação. Depois disso, o que vale é o que o usuário salvar.
 */
private object DadosIniciais : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        val hoje = LocalDate.now().toEpochDay()

        db.execSQL(
            "INSERT INTO clientes (nome, cpf, telefone, nascimento, ativo, dataCadastro) VALUES " +
                "('Maria Souza', '52998224725', '41998765432', '15031990', 1, ${hoje - 120}), " +
                "('João Pereira', '11144477735', '4133334444', '02111985', 1, ${hoje - 45}), " +
                "('Ana Lima', '12345678909', '', '', 0, ${hoje - 3})"
        )
        db.execSQL(
            "INSERT INTO fornecedores (razaoSocial, cnpj, categoria, prazoEntregaDias, observacoes, dataCadastro) VALUES " +
                "('Embalagens Paraná Ltda', '11222333000181', 'Embalagens', 5, 'Caixas de papelão e fitas adesivas.', ${hoje - 200}), " +
                "('Madeireira Araucária Ltda', '11444777000161', 'Matéria-prima', 10, 'Pinus tratado. Pedido mínimo de 2 m³.', ${hoje - 90}), " +
                "('Madeireira Araucária - Filial Curitiba', '11444777000242', 'Matéria-prima', 3, 'Retirada no balcão disponível.', ${hoje - 15})"
        )
    }
}
