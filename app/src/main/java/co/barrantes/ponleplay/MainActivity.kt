package co.barrantes.ponleplay

import android.app.PictureInPictureParams
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerView
import co.barrantes.ponleplay.playback.PlaybackService
import com.google.common.util.concurrent.MoreExecutors

private val Ink=Color(0xFF090909); private val Paper=Color(0xFFF6F2EA); private val Muted=Color(0xFFAAA39A); private val Amber=Color(0xFFF3B45F); private val Panel=Color(0xFF171513)

class MainActivity:ComponentActivity(){
 private var uri:Uri?=null; private var controller:MediaController?=null
 override fun onCreate(b:Bundle?){super.onCreate(b);WindowCompat.setDecorFitsSystemWindows(window,false);uri=getPreferences(0).getString("last_uri",null)?.let(Uri::parse);setContent{PonleplayScreen(uri,::select,::pip){controller=it}}}
 private fun select(u:Uri){try{contentResolver.takePersistableUriPermission(u,Intent.FLAG_GRANT_READ_URI_PERMISSION)}catch(_:SecurityException){};getPreferences(0).edit().putString("last_uri",u.toString()).apply();uri=u}
 private fun pip(){if(packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE))enterPictureInPictureMode(PictureInPictureParams.Builder().setAspectRatio(Rational(16,9)).build())}
 override fun onUserLeaveHint(){if(uri!=null&&contentResolver.getType(uri!!).orEmpty().startsWith("video/")&&controller?.isPlaying==true)pip();super.onUserLeaveHint()}
 override fun onDestroy(){controller?.release();super.onDestroy()}
}
@Composable private fun PonleplayScreen(initial:Uri?,onSelect:(Uri)->Unit,onPip:()->Unit,onReady:(MediaController)->Unit){
 val c=LocalContext.current;var name by remember{mutableStateOf(initial?.lastPathSegment?:"")};var video by remember{mutableStateOf(false)};var playing by remember{mutableStateOf(false)};var mc by remember{mutableStateOf<MediaController?>(null)}
 val future=remember(c){MediaController.Builder(c,SessionToken(c,ComponentName(c,PlaybackService::class.java))).buildAsync()}
 DisposableEffect(future){val r=Runnable{mc=runCatching{future.get()}.getOrNull();mc?.let(onReady)};future.addListener(r,MoreExecutors.directExecutor());onDispose{mc?.release()}}
 fun load(u:Uri){name=u.lastPathSegment?:"Contenido seleccionado";video=c.contentResolver.getType(u).orEmpty().startsWith("video/");ContextCompat.startForegroundService(c,Intent(c,PlaybackService::class.java).apply{data=u;addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)})}
 LaunchedEffect(initial){initial?.let(::load)}
 val picker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){u->u?.let{onSelect(it);load(it)}}
 DisposableEffect(mc){val l=object:Player.Listener{override fun onIsPlayingChanged(v:Boolean){playing=v}};mc?.addListener(l);onDispose{mc?.removeListener(l)}}
 MaterialTheme{Surface(Modifier.fillMaxSize(),color=Ink){Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(18.dp)){
  Row(Modifier.fillMaxWidth()){Text("PONLEPLAY",color=Amber,fontWeight=FontWeight.Bold,letterSpacing=1.2.sp);Spacer(Modifier.weight(1f));Text("MEDIA PLAYER",color=Muted,fontSize=11.sp)}
  Text("Tu contenido.\nA un toque.",color=Paper,style=MaterialTheme.typography.displaySmall)
  if(video&&mc!=null){Text(name,color=Muted,maxLines=1);Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(24.dp),colors=CardDefaults.cardColors(Color.Black)){AndroidView(factory={ctx->PlayerView(ctx).apply{useController=true;keepScreenOn=true;this.player=mc}},modifier=Modifier.fillMaxWidth().aspectRatio(16f/9f),update={view->view.player=mc})};Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly){TextButton({mc?.seekBack()}){Text("−10",color=Paper)};Button({if(playing)mc?.pause()else mc?.play()},shape=RoundedCornerShape(50),colors=ButtonDefaults.buttonColors(Amber,Ink)){Text(if(playing)"❚❚"else"▶",fontSize=22.sp)};TextButton({mc?.seekForward()}){Text("+10",color=Paper)}};Button(onPip,Modifier.align(Alignment.CenterHorizontally),colors=ButtonDefaults.buttonColors(Panel,Paper)){Text("PIP")}}
  else Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(24.dp),colors=CardDefaults.cardColors(Panel)){Column(Modifier.padding(22.dp)){Text("LISTO PARA REPRODUCIR",color=Amber,fontSize=12.sp,fontWeight=FontWeight.Bold);Text("Tu espacio multimedia",color=Paper,style=MaterialTheme.typography.titleLarge);Text("Selecciona un video o audio local.",color=Muted)}}
  Spacer(Modifier.weight(1f));Button({picker.launch(arrayOf("audio/*","video/*"))},Modifier.fillMaxWidth().height(56.dp),shape=RoundedCornerShape(18.dp),colors=ButtonDefaults.buttonColors(Amber,Ink)){Text(if(name.isEmpty())"Elegir contenido"else"Cambiar contenido",fontWeight=FontWeight.Bold)}
 }}}}
