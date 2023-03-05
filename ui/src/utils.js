import isEmpty from 'lodash/isEmpty'
import { DEBUG } from './constants'

export const debug = (message, ...args) => {
  if (DEBUG) {
    if (!isEmpty(args)) {
      console.log(message, args)
    } else {
      console.log(message)
    }
  }
}
