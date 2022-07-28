
# PodPlayer
[![forthebadge](https://forthebadge.com/images/badges/built-with-love.svg)](https://forthebadge.com)[![forthebadge](https://forthebadge.com/images/badges/built-for-android.svg)](https://forthebadge.com)[![forthebadge](https://forthebadge.com/images/badges/powered-by-coffee.svg)](https://forthebadge.com)

This app will allow searching and subscribing to podcasts from iTunes and provide a playback interface with speed controls.


## Screenshots

<img width="211" alt="1" src="https://user-images.githubusercontent.com/70475447/127272363-ad788823-d7ff-4445-86d9-9689b07c6e35.png"><img width="222" alt="2" src="https://user-images.githubusercontent.com/70475447/127272851-d2fc8926-1c90-463a-bfa7-e4aabf0ca0f8.png"><img width="219" alt="3" src="https://user-images.githubusercontent.com/70475447/127272845-bafadb60-bcd0-458a-9780-adb43f0592bf.png"><img width="216" alt="4" src="https://user-images.githubusercontent.com/70475447/127272831-027bad81-a7b6-41b6-b87a-a15cb0cb942a.png"><img width="221" alt="5" src="https://user-images.githubusercontent.com/70475447/127272836-a0324073-7d73-459e-a1a8-b04c30618167.png"><img width="217" alt="6" src="https://user-images.githubusercontent.com/70475447/127272849-8b4e9ad3-a8a5-4820-bfd8-123de088422f.png"><img width="537" alt="7" src="https://user-images.githubusercontent.com/70475447/127273587-758856a6-d9ce-414b-9ba3-a14b4039ce60.png">

## Getting started

Install my-project from

```bash

  https://github.com/Mr-Bakai/PodPlayer
  

```


## Features

- Supports Light/dark mode
- Subscription  to favorite podcasts
- Updates the user of new episodes of the subscribed podcasts
- Video Playback
- Audio Playback
- Playback controls outside of the app
- Podcast search 
    


## API Reference

#### API used for podcast search:

- Base URL 
```http
  https://itunes.apple.com
```
- End point
```http
  /search?media=podcast
```


#### Get all item

```http
  GET/term
```

| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `term`    | `string` | **Performs search by term**|


## Currently running OS

| OS        | Status               |
| :-------- | :-------             |
| `Android` | `Compatible`         |
| `iOS`     | `Not yet compatible` |


  
  ## Used Libraries 

- Room https://developer.android.com/training/data-storage/room
- Retrofit https://square.github.io/retrofit/
- Glide https://github.com/bumptech/glide
- Kotlin Extensions and Coroutines support for Room https://developer.android.com/kotlin/coroutines
- Koin DI  https://insert-koin.io
- Shimmer http://facebook.github.io/shimmer-android/



## Architecture 

- MVVM
- Repository pattern 
- Separation of concerns
- Drive UI from a model
- Base Fragment
- Base ViewModel
- The principle of providing the view with just enough information for presentation

