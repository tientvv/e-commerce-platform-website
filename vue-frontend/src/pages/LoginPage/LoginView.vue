<script setup></script>

<template>
  <div>
    <h2 v-if="user">Signed In User: {{ user }}</h2>
    <div v-if="isSignedIn">
      <button>Logout</button>
    </div>
    <div v-if="!isSignedIn">
      <h3>Google Login</h3>
      <button @click="handleGoogleLogin">Login</button>
    </div>
  </div>
</template>

<script>
import { GoogleAuthProvider, getAuth } from 'firebase/auth'

const provider = new GoogleAuthProvider()
const auth = getAuth()

export default {
  data() {
    return {
      user: '',
      isSignedIn: false,
    }
  },
  methods: {
    handleGoogleLogin() {
      signInWithPopup(auth, provider)
        .then((result) => {
          console.log(result)
        })
        .catch((error) => {
          console.log(error)
        })
    },
  },
}
</script>
