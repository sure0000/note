## insert value

v-once: only change once
v-html: html string can be work as html

mustache {{}} only aplly to content

## bind 

v-bind: apply to props
```html
<img v-bind:src="imgUrl" />
<img :src="imgUrl" />
```

v-bind bind class
```html
<!-- object -->
<h2 v-bind:class="{active: isActive, line: isLine}" class="title"> {{message}} </h2>
```

v-for
```html
<ul>
    <li v-for="item in items"></li>
</ul>

compute
```js
compute: {
    get:{},
    set: {},
    fullName: function() {
        <!-- just be called once -->
        return this.firstName + ' ' + this.lastName
    }
}
```

v-on: event listen, click , drag, keyup, etc
```html
<button v-on:click="add()"></button>
<button @click="add()"></button>

<!-- pass params -->
<!-- default pass event object -->
<button @click="add"></button>
<!-- don't pass object -->
<button @click="add()"></button>
<!-- pass event object by hand -->
<button @click="add(123, $event)"></button>
<!-- prevent default event -->
<input type='submit' @click.prevent="submit" value="submit"/>

```

v-if v-else-if v-else v-show
```html
<h2 v-if='true'> {{message}} </h2>
<h2 v-if-else-if='true'> {{message}} </h2>
<h2 v-else='true'> {{message}} </h2>
```

v-model: 双向绑定
```html
<!-- equal : v-bind:value + v-bind:input -->
<input type="text" v-model="message" />