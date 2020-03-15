npm install -g create-next-app

npx create-next-app demo

pages 目录下，next 自动路由，根据 pages 下的目录路径查找

## 路由跳转
```js
import React from 'react'
import Link from 'next/link'
import Router from 'next/router'
const Home = () => {
  function gotoA(){
    Router.push('/jspangA')
  }
  return(
    <>
      <div>我是首页</div>
      <div>
        <Link href="/jspangA">
          <a>
            <span>去JspangA页面</span>
            <span>前端博客</span>
          </a>
        </Link>
      </div>
      <div><Link href="/jspangB"><a>去JspangB页面</a></Link></div>
      <div>
        <button onClick={gotoA}>去JspangA页面</button>
      </div>
    </>
  )

}
export default Home
```

## 路由传值

**跳转**
```js
import React from 'react'
import Link from 'next/link'
import Router from 'next/router'
const Home = () => {
  return(
    <>
      <div>我是首页</div>
      <div>
        <Link href={{pathname:'/xiaojiejie',query:{name:'结衣'}}}><a>选结衣</a></Link><br/>
        <Link href="/xiaojiejie?name=苍井空"><a>选苍井空</a></Link>
      </div>
    </>
  )

}
export default Home
```
**接收**
```js
import { withRouter} from 'next/router'
import Link from 'next/link'

const Xiaojiejie = ({router})=>{
    return (
        <>
            <div>{router.query.name},来为我们服务了 .</div>
            <Link href="/"><a>返回首页</a></Link>
        </>
    )
}

export default withRouter(Xiaojiejie)
```

## 路由6个钩子函数
```js
  Router.events.on('routeChangeStart',(...args)=>{
    console.log('1.routeChangeStart->路由开始变化,参数为:',...args)
  })

   Router.events.on('routeChangeComplete',(...args)=>{
    console.log('routeChangeComplete->路由结束变化,参数为:',...args)
  })

   Router.events.on('beforeHistoryChange',(...args)=>{
    console.log('3,beforeHistoryChange->在改变浏览器 history之前触发,参数为:',...args)
  })

   Router.events.on('routeChangeError',(...args)=>{
    console.log('4,routeChangeError->跳转发生错误,参数为:',...args)
  })

  Router.events.on('hashChangeStart',(...args)=>{
    console.log('5,hashChangeStart->hash跳转开始时执行,参数为:',...args)
  })

  Router.events.on('hashChangeComplete',(...args)=>{
    console.log('6,hashChangeComplete->hash跳转完成时,参数为:',...args)
  })
  ```

## 页面初始化利用 getInitialProps + Axios 获取远程数据

```js
import { withRouter} from 'next/router'
import Link from 'next/link'
import axios from 'axios'

// 使用了 withRouter
const Xiaojiejie = ({router,list})=>{
    return (
        <>
            <div>{router.query.name},来为我们服务了 .<br/>{list}</div>
            <Link href="/"><a>返回首页</a></Link>
        </>
    )
}

Xiaojiejie.getInitialProps = async ()=>{
    const promise =new Promise((resolve)=>{
            axios('https://www.easy-mock.com/mock/5cfcce489dc7c36bd6da2c99/xiaojiejie/getList').then(
                (res)=>{
                    console.log('远程数据结果：',res)
                    resolve(res.data.data)
                }
            )
    })
    return await promise
}

export default withRouter(Xiaojiejie)
```

## lazy loading

**外部模块**
```js
import React, {useState} from 'react'
//删除import moment
function Time(){

    const [nowTime,setTime] = useState(Date.now())

    const changeTime= async ()=>{ //把方法变成异步模式
        const moment = await import('moment') //等待moment加载完成
        setTime(moment.default(Date.now()).format()) //注意使用defalut
    }
    return (
        <>
            <div>显示时间为:{nowTime}</div>
            <div><button onClick={changeTime}>改变时间格式</button></div>
        </>
    )
}
export default Time
```

**自定义组件**
```js
import React, {useState} from 'react'
import dynamic from 'next/dynamic'

// key code
const One = dynamic(import('../components/one'))

function Time(){

    const [nowTime,setTime] = useState(Date.now())

    const changeTime= async ()=>{
        const moment = await import('moment')

        setTime(moment.default(Date.now()).format())
    }
    return (
        <>
            <div>显示时间为:{nowTime}</div>
            <One/>
            <div><button onClick={changeTime}>改变时间格式</button></div>
        </>
    )
}
export default Time
```

## Head SEO

```js
import Head from 'next/head'
function Header(){ 
    return (
        <>
            <Head>
                <title>技术胖是最胖的！</title>
                <meta charSet='utf-8' />
            </Head>
            <div>JSPang.com</div>

        </> 
    )
}
export default Header
```

## nextJs 配置 antd

**配置 antd**
```
yarn add @zeit/next-css

// next.config.js
const withCss = require('@zeit/next-css')

if(typeof require !== 'undefined'){
    require.extensions['.css']=file=>{}
}

module.exports = withCss({})
```

**按需加载 antd**

```
yarn add babel-plugin-import

// .babelrc
{
    "presets":["next/babel"],  //Next.js的总配置文件，相当于继承了它本身的所有配置
    "plugins":[     //增加新的插件，这个插件就是让antd可以按需引入，包括CSS
        [
            "import",
            {
                "libraryName":"antd"
            }
        ]
    ]
}
```

## nextJS + antd 打包问题

其实Next.js大打包时非常简单的，只要一个命令就可以打包成功。但是当你使用了Ant Desgin后，在打包的时候会遇到一些坑。

在page目录下，新建一个_app.js文件，然后写入下面的代码。

```js
import App from 'next/app'

import 'antd/dist/antd.css'

export default App
```