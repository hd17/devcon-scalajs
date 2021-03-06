package example

import java.time.Instant

import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import org.scalajs.dom
import shared.component.PageIds._
import shared.component.{Components, Speaker, Talk}

import scalatags.JsDom.all._
import scalatags.JsDom.tags2._

object ScalaJSExample {

  object JsDomComponents extends Components(scalatags.JsDom)
  import JsDomComponents._

  def main(args: Array[String]): Unit = {
    refreshTalks.runAsync

    val newTalkName = input(cls := "form-control").render
    val speakerName = input(cls := "form-control").render

    val addTalkButton = button(
      cls := "btn btn-primary",
      onclick := { () =>
        val newTalk =
          Talk(None, newTalkName.value, Speaker(speakerName.value), Instant.now, Instant.now)

        for {
          _ <- AgendaApi.createTalkRequest(newTalk)
          _ <- refreshTalks
        } {}
      }
    )("add new Talk")

    val addTalkSection = section(
      div(cls := "form-group")(label("talk name: "), newTalkName),
      div(cls := "form-group")(label("speaker name: "), speakerName),
      addTalkButton
    )

    val entrypoint = dom.document.getElementById(mainId)
    entrypoint.appendChild(addTalkSection.render)
  }

  val refreshTalks: Task[_] = {
    val taskList = dom.document.getElementById(talkListId)

    AgendaApi.talksRequest.map { talks =>
      taskList.innerHTML = ""
      val talkNodes = for (t <- talks) yield {
        div(cls := "card", scalatags.JsDom.all.style := "width: 20rem; margin: 1rem")(
          img(cls := "card-img-top", src := "http://via.placeholder.com/300x200"),
          div(cls := "card-body")(
            h4(cls := "card-title")(t.name),
            p(cls := "card-text")(s"by ${t.speaker.name} on ${t.startTime}"),
            a(cls := "btn btn-danger", onclick := { () =>
              AgendaApi.deleteTalkRequest(t).runAsync
              refreshTalks.runAsync
            })("Delete")
          )
        )
      }
      taskList.appendChild(scalatags.JsDom.all.SeqFrag(talkNodes).render)
    }
  }

}
