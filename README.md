# 📝 ToDo List com Firebase

Aplicativo Android de lista de tarefas (ToDo List) desenvolvido em Kotlin com Jetpack Compose, integrado ao Firebase para autenticação de usuários e armazenamento em nuvem. Este projeto demonstra a implementação de autenticação, persistência de dados em nuvem, gerenciamento de estado avançado e navegação moderna no Android.

## 🎯 Objetivo

Este projeto foi desenvolvido como trabalho acadêmico com os seguintes objetivos:
- Integração de aplicativo Android com serviços de cloud (Firebase)
- Implementação de autenticação de usuários
- Gerenciamento avançado de estado com arquitetura moderna
- Aprofundamento em Jetpack Compose e navegação

## ✨ Funcionalidades

### Autenticação
- **Login**: Permite que usuários façam login com email e senha
- **Cadastro (Sign Up)**: Criação de novos usuários
- **Logout**: Encerramento seguro da sessão
- **Validação**: Verificação de credenciais via Firebase Authentication

### Gerenciamento de Tarefas
- **Criar tarefa**: Adicionar novas tarefas com título e descrição
- **Editar tarefa**: Modificar tarefas existentes
- **Marcar como completa**: Alternar status de conclusão
- **Excluir tarefa**: Remover tarefas da lista
- **Persistência por usuário**: Cada usuário visualiza apenas suas próprias tarefas
- **Sincronização em tempo real**: Atualizações automáticas via Firestore listeners

## 🖼️ Telas do Aplicativo

### 1. Tela de Autenticação (AuthScreen)
- **Localização**: `app/src/main/java/com/example/todolist/ui/auth/AuthScreen.kt`
- **Funcionalidades**:
  - Alternância entre modo Login e Cadastro
  - Campos de email e senha com validação
  - Confirmação de senha no modo cadastro
  - Visibilidade de senha configurável
  - Feedback de erros e mensagens de sucesso
  - Loading state durante autenticação

### 2. Tela de Lista de Tarefas (ListScreen)
- **Localização**: `app/src/main/java/com/example/todolist/ui/feature/list/ListScreen.kt`
- **Funcionalidades**:
  - Exibição de todas as tarefas do usuário logado
  - Email do usuário na TopBar
  - Botão de logout
  - Mensagem de boas-vindas após login
  - Navegação para tela de criação/edição
  - Checkbox para marcar tarefas como completas
  - Swipe para deletar tarefas
  - Lista vazia com mensagem informativa

### 3. Tela de Adicionar/Editar Tarefa (AddEditScreen)
- **Localização**: `app/src/main/java/com/example/todolist/ui/feature/addedit/AddEditScreen.kt`
- **Funcionalidades**:
  - Formulário com campo de título (obrigatório)
  - Campo de descrição (opcional)
  - Floating Action Button para salvar
  - Validação de campos
  - Feedback via Snackbar

## 🏗️ Arquitetura

O projeto segue os princípios da **arquitetura MVVM (Model-View-ViewModel)** com **Clean Architecture**, organizado em camadas:

### Camadas da Aplicação

#### 1. **UI Layer** (Presentation)
- **ViewModels**: Gerenciam o estado da UI e lógica de apresentação
  - `AuthViewModel`: Gerencia autenticação
  - `ListViewModel`: Gerencia lista de tarefas
  - `AddEditViewModel`: Gerencia criação/edição
- **Composables**: Componentes de interface em Jetpack Compose
- **UI States**: Data classes que representam o estado da UI
- **Events**: Sealed interfaces para ações do usuário

#### 2. **Domain Layer**
- **Models**: Entidades de domínio (`Todo`)
- Representação agnóstica de plataforma

#### 3. **Data Layer**
- **Repositories**: Abstração de fontes de dados
  - `AuthRepository`: Interface para autenticação
  - `TodoRepository`: Interface para operações de tarefas
- **Repository Implementations**:
  - `AuthRepositoryImpl`: Implementação com Firebase Auth
  - `FirestoreTodoRepository`: Implementação com Firestore
- **Data Sources**:
  - Firebase Authentication
  - Cloud Firestore
  - Room Database (estrutura presente, mas não utilizada atualmente)

#### 4. **Dependency Injection** (DI)
- **Dagger Hilt** para injeção de dependências
- Módulos:
  - `FirebaseModule`: Provê instâncias Firebase
  - `DatabaseModule`: Provê repositórios e DAOs

### Padrões Utilizados

- **Repository Pattern**: Abstração de fontes de dados
- **Single Source of Truth**: Firestore como fonte única de verdade
- **Unidirectional Data Flow**: Fluxo de dados em uma direção (UI → ViewModel → Repository)
- **State Management**: `StateFlow` e `collectAsStateWithLifecycle()`
- **Separation of Concerns**: Cada camada tem responsabilidade única

## 📊 Modelo de Dados

### Entidade Principal: Todo

```kotlin
data class Todo(
    val id: String,
    val title: String,
    val description: String?,
    val isCompleted: Boolean
)
```

### Modelo Firestore: FirestoreTodo

```kotlin
data class FirestoreTodo(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val description: String? = null,
    @get:PropertyName("isCompleted")
    @set:PropertyName("isCompleted")
    var isCompleted: Boolean = false,
    val userId: String = "",
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    var updatedAt: Date? = null
)
```

**Campos**:
- `id`: Identificador único gerado pelo Firestore
- `title`: Título da tarefa (obrigatório)
- `description`: Descrição detalhada (opcional)
- `isCompleted`: Status de conclusão
- `userId`: ID do usuário proprietário (para filtrar tarefas por usuário)
- `createdAt`: Timestamp de criação (gerenciado pelo servidor)
- `updatedAt`: Timestamp de última atualização (gerenciado pelo servidor)

### Modelo Room: TodoEntity

```kotlin
@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String?,
    val isCompleted: Boolean,
    val userId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
```

## 💾 Persistência de Dados

### Firebase Firestore

A persistência principal é implementada através do **Cloud Firestore**:

#### Estrutura da Coleção
```
todos (collection)
  └── {documentId}
      ├── title: String
      ├── description: String?
      ├── isCompleted: Boolean
      ├── userId: String
      ├── createdAt: Timestamp
      └── updatedAt: Timestamp
```

#### Implementação

**1. Operações CRUD**:

```kotlin
class FirestoreTodoRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : TodoRepository {
    
    // Inserir/Atualizar
    override suspend fun insert(title: String, description: String?, id: String?)
    
    // Atualizar status de conclusão
    override suspend fun updateCompleted(id: String, isCompleted: Boolean)
    
    // Deletar
    override suspend fun delete(id: String)
    
    // Observar mudanças em tempo real
    override fun getAll(): Flow<List<Todo>>
}
```

**2. Sincronização em Tempo Real**:
- Utiliza `addSnapshotListener` do Firestore para updates automáticos
- Filtro por `userId` para isolamento de dados por usuário
- `Flow` reativo com `callbackFlow` para emitir atualizações

**3. Segurança**:
- Validação de propriedade: Verifica se o `userId` da tarefa corresponde ao usuário autenticado
- Todas as operações requerem autenticação válida

### Firebase Authentication

**Funcionalidades implementadas**:
- Cadastro com email/senha: `createUserWithEmailAndPassword()`
- Login: `signInWithEmailAndPassword()`
- Logout: `signOut()`
- Monitoramento de estado: `AuthStateListener` para observar mudanças de autenticação

**Implementação**:

```kotlin
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {
    
    override val authState: Flow<AuthState> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(AuthState(
                isAuthenticated = auth.currentUser != null,
                user = auth.currentUser,
                isLoading = false
            ))
        }
        firebaseAuth.addAuthStateListener(authStateListener)
        awaitClose { firebaseAuth.removeAuthStateListener(authStateListener) }
    }
}
```

### Room Database

O projeto inclui a estrutura do Room Database (`TodoDatabase`, `TodoDao`, `TodoEntity`), preparada para:
- Cache offline
- Sincronização posterior
- Fallback em caso de falta de conectividade

## 🛠️ Tecnologias e Bibliotecas

### Core
- **Kotlin**: Linguagem principal
- **Jetpack Compose**: Framework de UI declarativa

### Firebase
- **Firebase Authentication**: Autenticação de usuários
- **Cloud Firestore**: Banco de dados NoSQL em tempo real
- **Firebase BOM**: Gerenciamento de versões Firebase

### Arquitetura & DI
- **Dagger Hilt**: Injeção de dependências
- **ViewModel**: Gerenciamento de estado com lifecycle
- **Navigation Compose**: Navegação entre telas
- **Kotlinx Serialization**: Serialização type-safe para navegação

### Persistência Local
- **Room**: SQLite abstraction layer (estrutura preparada)

## 📦 Estrutura do Projeto

```
app/src/main/java/com/example/todolist/
├── auth/                          # Módulo de autenticação
│   ├── AuthRepository.kt          # Interface do repositório
│   ├── AuthRepositoryImpl.kt      # Implementação com Firebase
│   ├── AuthResult.kt              # Sealed class de resultados
│   └── AuthState.kt               # Estado de autenticação
├── data/                          # Camada de dados
│   ├── FirestoreTodoRepository.kt # Repositório Firestore
│   ├── TodoDatabase.kt            # Room database
│   ├── TodoDao.kt                 # Data Access Object
│   ├── TodoEntity.kt              # Entidades (Room e Firestore)
│   └── TodoRepository.kt          # Interface do repositório
├── di/                            # Dependency Injection
│   ├── DatabaseModule.kt          # Módulo de database
│   └── FirebaseModule.kt          # Módulo Firebase
├── domain/                        # Camada de domínio
│   └── Todo.kt                    # Modelo de domínio
├── navigation/                    # Navegação
│   └── TodoNavHost.kt             # NavHost e rotas
├── ui/                            # Camada de apresentação
│   ├── auth/                      # Tela de autenticação
│   │   ├── AuthScreen.kt
│   │   ├── AuthViewModel.kt
│   │   └── AuthEvent.kt
│   ├── feature/
│   │   ├── list/                  # Lista de tarefas
│   │   │   ├── ListScreen.kt
│   │   │   ├── ListViewModel.kt
│   │   │   └── ListEvent.kt
│   │   └── addedit/               # Adicionar/Editar
│   │       ├── AddEditScreen.kt
│   │       ├── AddEditViewModel.kt
│   │       └── AddEditEvent.kt
│   ├── components/                # Componentes reutilizáveis
│   ├── theme/                     # Tema do app
│   └── UiEvent.kt                 # Eventos de navegação
└── MainActivity.kt                # Activity principal
```

## 🚀 Como Executar

### Pré-requisitos
1. Android Studio Iguana ou superior
2. JDK 11 ou superior
3. Projeto Firebase configurado

### Configuração Firebase

1. Acesse [Firebase Console](https://console.firebase.google.com/)
2. Crie um novo projeto ou use um existente
3. Adicione um app Android com o package name: `com.example.todolist`
4. Baixe o arquivo `google-services.json`
5. Coloque o arquivo em `app/google-services.json`
6. Habilite **Authentication** (Email/Password)
7. Habilite **Cloud Firestore** e configure as regras:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /todos/{todoId} {
      allow read, write: if request.auth != null && 
                          request.resource.data.userId == request.auth.uid;
    }
  }
}
```

### Executar o Projeto

1. Clone o repositório:
```bash
git clone https://github.com/BTeo08/todo-list-firebase.git
cd todo-list-firebase
```

2. Abra o projeto no Android Studio

3. Sincronize as dependências Gradle

4. Execute no emulador ou dispositivo físico

## 🔄 Fluxo da Aplicação

1. **Inicialização**: App verifica se há usuário autenticado
2. **Não autenticado**: Exibe `AuthScreen` (Login/Cadastro)
3. **Autenticação bem-sucedida**: Navega para `ListScreen`
4. **ListScreen**: 
   - Carrega tarefas do Firestore filtradas por `userId`
   - Observa mudanças em tempo real
   - Permite criar, editar, completar e deletar tarefas
5. **AddEditScreen**: Formulário para criar/editar tarefas
6. **Logout**: Limpa autenticação e retorna para `AuthScreen`

## 📈 Melhorias Futuras

### 1. Cache Offline
- **Implementação**: Ativar o Room Database já estruturado no projeto
- **Benefício**: Permitir uso offline com sincronização posterior
- **Estratégia**: Implementar padrão Repository com duas fontes (local + remote)

### 2. Validação de Formulários
- **Email**: Regex para validar formato de email
- **Senha**: Requisitos mínimos (comprimento, caracteres especiais)
- **Feedback**: Mensagens de erro inline nos campos

### 3. Categorização de Tarefas
- **Tags/Labels**: Adicionar categorias às tarefas
- **Filtros**: Filtrar por categoria, prioridade, data
- **Cores**: Identificação visual por categoria

### 4. Notificações
- **Push Notifications**: Lembretes de tarefas pendentes
- **Firebase Cloud Messaging**: Implementar FCM para notificações

### 5. Data de Vencimento
- **Deadlines**: Adicionar campo de data limite
- **Alertas**: Notificar tarefas próximas do vencimento
- **Ordenação**: Ordenar por data de vencimento

### 6. Busca e Ordenação
- **Busca**: Campo de texto para buscar tarefas por título/descrição
- **Ordenação**: Por data, alfabética, status de conclusão
- **Filtros avançados**: Combinar múltiplos critérios

### 7. Temas
- **Dark Mode**: Suporte completo ao tema escuro
- **Personalização**: Permitir escolha de cores/temas

### 8. Compartilhamento
- **Tarefas compartilhadas**: Permitir colaboração entre usuários
- **Permissões**: Leitura/escrita granular

### 9. Testes
- **Unit Tests**: Testar ViewModels e Repositories
- **UI Tests**: Testar fluxos de navegação com Compose Testing
- **Integration Tests**: Testar integração com Firebase

### 10. Performance
- **Paginação**: Implementar paginação para listas grandes
- **Cache de imagens**: Se adicionar fotos às tarefas
- **Otimização de queries**: Índices no Firestore

## 📝 Decisões de Arquitetura Importantes

### 1. Firestore vs Room como Fonte Principal
**Decisão**: Usar Firestore como fonte única de verdade
**Motivo**: 
- Sincronização automática entre dispositivos
- Backend gerenciado (sem necessidade de servidor próprio)
- Listeners em tempo real nativos
- Escalabilidade automática

### 2. MVVM com Clean Architecture
**Decisão**: Separação em camadas (UI, Domain, Data)
**Motivo**:
- Testabilidade: Cada camada pode ser testada isoladamente
- Manutenibilidade: Mudanças em uma camada não afetam outras
- Reutilização: Lógica de negócio independente da UI

### 3. Dagger Hilt para DI
**Decisão**: Usar Hilt ao invés de injeção manual ou Koin
**Motivo**:
- Integração oficial com Jetpack
- Compile-time safety
- Menor boilerplate comparado ao Dagger puro
- Suporte a ViewModels e WorkManager

### 4. Jetpack Compose
**Decisão**: UI totalmente em Compose (sem XML)
**Motivo**:
- Declarativo e reativo
- Menos boilerplate
- Melhor integração com Kotlin
- Futuro do desenvolvimento Android

### 5. Navigation Compose com Type-Safe Routes
**Decisão**: Usar Kotlinx Serialization para navegação type-safe
**Motivo**:
- Segurança de tipos em compile-time
- Menos erros de navegação
- Autocomplete e refatoração facilitados

## 👨‍💻 Autoras
- [Bruna Teodoro](https://github.com/BTeo08)
- [Tainá Peixoto](https://github.com/peixotots)


**Tecnologias**: Kotlin • Jetpack Compose • Firebase • MVVM • Clean Architecture • Dagger Hilt • Coroutines & Flow
