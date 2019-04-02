#### 什么是协程（coroutines）

---

#### 协程简介

* 协程就像是轻量级的线程，但不是线程
* 协程可以使用同步的方法编写异步代码
* 多个协程可以共用一个线程
* 协程实现的基础是可中断方法（又称为挂起函数）

---

#### 可中断方法（suspending functions）

---

顾名思义，`可中断方法`可以中断协程的执行

>可中断方法只能运行在协程或者其他可中断方法中，通常情况下不会阻塞当前线程，使用`suspend`来修饰

``` kotlin
suspend fun plus(a: Int, b: Int): Int = a + b
```

---

#### 启动协程
* runBlocking
* launch
* async

---

#### runBlocking

* 直接阻塞当前线程，直到协程中的代码执行完毕
* 一般只在测试可中断方法时使用，生产环境基本用不到

```kotlin
fun main() {
    println("coroutine start")
    testPlus()
    println("coroutine end")
}

fun testPlus() = runBlocking {
    val result = plus(1, 1)
    println("result = $result")
}
```

输出结果：

coroutine start
result = 2
coroutine end

---

#### launch
* 不阻塞当前线程，经常会使用它来创建协程
* 需要一个作用域（`scope`）来调用`launch`方法
* 返回一个`Job`，Job继承自协程上下文（`CoroutineContext`）
* Job.join 可中断方法；中断当前协程直到所有子Job完成
* Job.cancel 普通方法；可取消与其关联的子Job

---
前面讲到需要一个作用域来调用launch方法，这里暂时使用`GlobalScope`，即全局作用域

```kotlin
val job = GlobalScope.launch {

    doJob()

    val result1 = doJob1()
    val result2 = doJob2()

    process(result1, result2)

}

job.join()
```

---
```kotlin
val job = GlobalScope.launch(Dispatchers.Main) {

    doJob()

    val result1 = doJob1()
    val result2 = doJob2()

    process(result1, result2)

}

job.cancel()
```
当`doJob1`执行时，job调用cancel方法，result1将不会返回，`doJob2`也不会被执行

---
#### async
* 可以并行的执行多个子任务，通常在协程中调用
* 不是中断方法，启动协程的同时，后面的代码会立即执行
* 返回一个特殊的Job，叫`Deferred`
* `Deferred`有一个叫`await`的方法，该方法是可中断方法，最终会返回结果

```kotlin
GlobalScope.launch() {

    val result = doJob()
    val result1 = async { doJob1(result) }
    val result2 = async { doJob2(result) }

    val finalResult = result1.await()  + result2.await()

}
```

---

#### 协程上下文（Context）

---
***刚开始的时候，我们说协程是轻量级的线程，那么她究竟运行在哪个线程呢？***

> 其实在调用launch或async方法时我们可以通过传入`dispatcher`来决定协程运行在哪个线程，也可以通过`withContext`切换线程。而`dispatcher`实现了协程上下文

---
#### CoroutineContext
> 协程上下文是一系列规则和配置的集合，即包含了一系列的键值对
#### Dispatcher
* `dispatcher`是协程上下文中的一个配置，可以指定协程运行的线程
* withContext是一个可中断方法，经常用来做线程调度
* `Default`(CPU密集型)、`IO`、`Main`(在Android中是UI线程)、`UnConfined`(不受控制的线程，一般不要轻易使用)
---

#### 协程作用域（Scope）
* `GlobalScope`是一个全局作用域，如果协程的运行周期与App生命周期一样长的情况下可以使用，其他情况不要使用。
* 通过实现`CoroutineScope`接口来自定义作用域，需要重写`coroutineContext`属性
```kotlin
class MainActivity : AppCompatActivity(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var job: Job

}
```
>通过kotlin的操作符重载我们可以使用`+`来组合两个不同类型的协程上下文。

`Dispatchers.Main`用来指定默认的dispatcher
`job`用来在需要的时机取消协程的执行

---

#### 协程与回调
***如何把现有的回调风格转化为协程代码呢？***
```kotlin
suspend fun suspendLogin(username: String, password: String): User =
    suspendCoroutine { continuation ->
        userService.doLogin(username, password) { user ->
            continuation.resume(user)
        }
    }
```
> `suspendCoroutine`方法返回一个`continuation`对象，用于返回回调的结果，只需调用`resume`方法即可。
`suspendCancellableCoroutine`与`suspendCoroutine`类似但是支持取消操作
---

#### 协程与RxJava

***协程可以替代Rxjava吗***

> 可以也不可以

* 如果只是简单的线程调度，以及实现简单的流式操作，完全可以
* 如果是复杂的流式编程，RxJava显然更合适
