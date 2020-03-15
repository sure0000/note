```javascript
import React, { useState } from 'react';
function Example2(){
    const [ age , setAge ] = useState(18)
    const [ sex , setSex ] = useState('男')
    const [ work , setWork ] = useState('前端程序员')
    return (
        <div>
            <p>JSPang 今年:{age}岁</p>
            <p>性别:{sex}</p>
            <p>工作是:{work}</p>

        </div>
    )
}
export default Example2;
```

> 在使用useState的时候只赋了初始值，并没有绑定任何的key,那React是怎么保证这三个useState找到它自己对应的state呢？
答案是：React是根据useState出现的顺序来确定的

## useEffect

**componentDidMonut and componentDidUpdate**
```javascript
// React首次渲染和之后的每次渲染都会调用一遍useEffect函数，而之前我们要用两个生命周期函数分别表示首次渲染(componentDidMonut)和更新导致的重新渲染(componentDidUpdate)。

// useEffect中定义的函数的执行不会阻碍浏览器更新视图，也就是说这些函数时异步执行的，而componentDidMonut和componentDidUpdate中的代码都是同步执行的。个人认为这个有好处也有坏处吧，比如我们要根据页面的大小，然后绘制当前弹出窗口的大小，如果时异步的就不好操作了。

import React, { useState , useEffect } from 'react';
function Example(){
    const [ count , setCount ] = useState(0);
    //---关键代码---------start-------
    useEffect(()=>{
        console.log(`useEffect=>You clicked ${count} times`)
    })
    //---关键代码---------end-------
    return (
        <div>
            <p>You clicked {count} times</p>
            <button onClick={()=>{setCount(count+1)}}>click me</button>
        </div>
    )
}
export default Example;
```

**componentWillUnmount**

```javascript
// 那到底要如何实现类似componentWillUnmount的效果那?这就需要请出useEffect的第二个参数，它是一个数组，数组中可以写入很多状态对应的变量，意思是当状态值发生变化时，我们才进行解绑。但是当传空数组[]时，就是当组件将被销毁时才进行解绑，这也就实现了componentWillUnmount的生命周期函数

function Index() {
    useEffect(()=>{
        console.log('useEffect=>老弟你来了！Index页面')
        return ()=>{
            console.log('老弟，你走了!Index页面')
        }
    },[])
    return <h2>JSPang.com</h2>;
}

// 这时候只要count状态发生变化，都会执行解绑副作用函数，浏览器的控制台也就打印出了一串
function Example(){
    const [ count , setCount ] = useState(0);
    useEffect(()=>{
        console.log(`useEffect=>You clicked ${count} times`)

        return ()=>{
            console.log('====================')
        }
    },[count])
    return (
        <div>
            <p>You clicked {count} times</p>
            <button onClick={()=>{setCount(count+1)}}>click me</button>

            <Router>
                <ul>
                    <li> <Link to="/">首页</Link> </li>
                    <li><Link to="/list/">列表</Link> </li>
                </ul>
                <Route path="/" exact component={Index} />
                <Route path="/list/" component={List} />
            </Router>
        </div>
    )
}
```

## useContext

```javascript
import React, { useState , createContext , useContext } from 'react';

// 子组件
function Counter(){
    const count = useContext(CountContext)  //一句话就可以得到count
    return (<h2>{count}</h2>)
}
//===关键代码
const CountContext = createContext()

function Example4(){
    const [ count , setCount ] = useState(0);

    return (
        <div>
            <p>You clicked {count} times</p>
            <button onClick={()=>{setCount(count+1)}}>click me</button>
            {/*======关键代码 */}
            <CountContext.Provider value={count}>
                <Counter />
            </CountContext.Provider>


        </div>
    )
}
export default Example4;
```

## useReducer + useContext = redux

**color.js**
```javascript
import React, { createContext, useReducer } from 'react';

export const ColorContext = createContext({})

const reducer = (state, action) => {
    switch(action.type) {
        case: UPDATE_COLOR:
            return action.color
        default:
            return state
    }
}

export const Color = props=>{
    const [ color, dispatch ] = useReducer(reducer, 'bule')
    return (
        <ColorContext.Provider value= {{color, dispatch}}>
            {props.children}
        </ColorContext.Provider>
    )
}
```

**buttons.js**
```js
import React , {useContext} from 'react'
import {ColorContext, UPDATE_COLOR} from './color'

function Buttons() {
    const {dispatch} = useContext(ColorContext)
    <Button onClick={()=>{dispatch({type: UPDATE_COLOR, color: "red"})}}> red </Button>
}

export default Buttons
```

**showArea.js**
```js
import React , { useContext } from 'react';
import { ColorContext } from './color';

function ShowArea(){
    const {color} = useContext(ColorContext)
    return (<div style={{color:color}}>字体颜色为{color}</div>)

}

export default ShowArea
```

## useMemo

```js
// useMemo主要用来解决使用React hooks产生的无用渲染的性能问题。使用function的形式来声明组件，失去了shouldCompnentUpdate（在组件更新之前）这个生命周期，也就是说我们没有办法通过组件更新前条件来决定组件是否更新。而且在函数组件中，也不再区分mount和update两个状态，这意味着函数组件的每一次调用都会执行内部的所有逻辑，就带来了非常大的性能损耗。
import React , {useState,useMemo} from 'react';

function Example7(){
    const [xiaohong , setXiaohong] = useState('小红待客状态')
    const [zhiling , setZhiling] = useState('志玲待客状态')
    return (
        <>
            <button onClick={()=>{setXiaohong(new Date().getTime())}}>小红</button>
            <button onClick={()=>{setZhiling(new Date().getTime()+',志玲向我们走来了')}}>志玲</button>
            <ChildComponent name={xiaohong}>{zhiling}</ChildComponent>
        </>
    )
}

function ChildComponent({name,children}){
    function changeXiaohong(name){
        console.log('她来了，她来了。小红向我们走来了')
        return name+',小红向我们走来了'
    }

    // key code
    const actionXiaohong = useMemo(()=>changeXiaohong(name),[name]) 
    return (
        <>
            <div>{actionXiaohong}</div>
            <div>{children}</div>
        </>
    )
}
```